import dataclasses
import typing

import psycopg_pool
from psycopg.rows import dict_row
import rapidjson
import os

# --- APPLICATION GLOBAL CONTEXT ----------------------------------------------
connection_string = os.getenv("DB_CONNECTION_STRING")
pool = psycopg_pool.ConnectionPool(connection_string, open=False, min_size=1, num_workers=1)
pool.open(wait=True)

# --- APPLICATION GLOBAL PROTOCOLS --------------------------------------------
status_map = {
    200: "OK",
    201: "Created",
    400: "Bad Request",
    404: "Not Found",
    500: "Internal Server Error"
}

HEADERS_LIST = typing.List[typing.Tuple[str, str]]
WSGI_ENVIRON = typing.Dict[str, object]
CONNECTION_POOL = psycopg_pool.ConnectionPool

HTTP_ENTITY_ANSWER = typing.Union[
    typing.Tuple[None, typing.Literal[404]],
    typing.Tuple[str, typing.Literal[200, 201]]
]


class HTTP_ENTITY(typing.Protocol):
    def __call__(self, environ: WSGI_ENVIRON, headers: HEADERS_LIST, pool: CONNECTION_POOL) -> HTTP_ENTITY_ANSWER:
        ...


# --- HTTP ENTITIES -----------------------------------------------------------
def not_found(environ: WSGI_ENVIRON, headers: HEADERS_LIST, pool: CONNECTION_POOL) -> HTTP_ENTITY_ANSWER:
    return None, 404


@dataclasses.dataclass(frozen=True)
class NewOrder:
    def __call__(self, environ: WSGI_ENVIRON, headers: HEADERS_LIST, pool: CONNECTION_POOL) -> HTTP_ENTITY_ANSWER:
        with pool.connection() as connection:
            connection.autocommit = True
            with connection.cursor() as cursor:
                cursor.execute(
                    "SELECT "
                    "id, price "
                    "FROM public.product "
                )
                product_records = cursor.fetchall()

        entry_records = rapidjson.load(environ.get('wsgi.input'))
        total = NewOrder.total_of_entries(product_records, entry_records)

        with pool.connection() as connection:
            with connection.cursor() as cursor:
                cursor.execute(
                    "INSERT INTO public.shoppingcart "
                    "(total) "
                    "VALUES "
                    "(%s) "
                    "RETURNING id",
                    params=[total]
                )
                order_id, = cursor.fetchone()
                with cursor.copy("COPY public.productorder (shoppingcart_id, product_id, amount) FROM STDIN") as copy:
                    for entry in entry_records:
                        copy.write_row((order_id, entry['productId'], entry['amount']))

        headers.append(('Content-Type', 'application/json'))
        return rapidjson.dumps(order_id), 201

    @staticmethod
    def total_of_entries(product_records, entry_records):
        product_records = {
            rec[0]: rec[1]
            for rec in product_records
        }
        return sum(
            (
                entry['amount'] * product_records[entry['productId']]
                for entry in entry_records
            ), 0.
        )


@dataclasses.dataclass(frozen=True)
class ViewProduct:
    id: bytes

    def __call__(self, environ: WSGI_ENVIRON, headers: HEADERS_LIST, pool: CONNECTION_POOL) -> HTTP_ENTITY_ANSWER:
        id = int(self.id)
        with pool.connection() as connection:
            connection.autocommit = True
            with connection.cursor(row_factory=dict_row) as cursor:
                cursor.execute(
                    "SELECT "
                    "id, name, price, description "
                    "FROM public.product "
                    "where id=%s",
                    params=[id]
                )
                product = cursor.fetchone()

        headers.append(('Content-Type', 'application/json'))
        return rapidjson.dumps(product), 200


def view_product_information(environ: WSGI_ENVIRON, headers: HEADERS_LIST, protocol: CONNECTION_POOL) -> HTTP_ENTITY_ANSWER:
    with pool.connection() as connection:
        connection.autocommit = True
        with connection.cursor(row_factory=dict_row) as cursor:
            cursor.execute(
                "SELECT "
                "id, name "
                "FROM public.product "
            )
            products = cursor.fetchall()

    headers.append(('Content-Type', 'application/json'))
    return rapidjson.dumps(products), 200


@dataclasses.dataclass(frozen=True)
class ViewOrder:
    id: bytes

    def __call__(self, environ: WSGI_ENVIRON, headers: HEADERS_LIST, pool: CONNECTION_POOL) -> HTTP_ENTITY_ANSWER:
        id = int(self.id)
        with pool.connection() as connection:
            connection.autocommit = True
            with connection.pipeline():
                with connection.cursor() as cursor:
                    cursor.execute(
                        "SELECT "
                        "total "
                        "FROM public.shoppingcart "
                        "WHERE id=%s",
                        params=[id]
                    )
                    cursor.execute(
                        "SELECT "
                        "amount, product_id "
                        "from public.productorder "
                        "where shoppingcart_id=%s",
                        params=[id]
                    )

                    total, = cursor.fetchone()
                    cursor.nextset()
                    order_entries = cursor.fetchall()

        headers.append(('Content-Type', 'application/json'))
        return rapidjson.dumps(dict(
            id=id,
            total=total,
            items=list(
                dict(
                    amount=record[0],
                    productId=record[1]
                )
                for record in order_entries
            )
        )), 200


# --- APPLICATION -------------------------------------------------------------
def entity_of(environ: WSGI_ENVIRON) -> HTTP_ENTITY:
    url = str(environ.get('PATH_INFO')).encode('ascii')
    segments = (seg for seg, _ in zip(
        (segment for segment in url.split(b'/') if segment),
        range(5)
    ))
    try:
        cursor = next(segments)
        if cursor == b'products':
            try:
                return ViewProduct(id=next(segments))
            except StopIteration:
                return view_product_information
        elif cursor == b'orders':
            cursor = next(segments)
            if cursor == b'new':
                return NewOrder()
            else:
                return ViewOrder(id=cursor)
    except StopIteration: pass
    return not_found


def response_of(environ):
    response_headers = list()
    data = None
    try:
        entity = entity_of(environ)
        data, status = entity(environ, response_headers, pool)
    except psycopg.Error:
        status = 500
    except Exception:
        status = 400

    status = "{} {}".format(status, status_map[status])
    if data is None:
        data = b''
    else:
        data = data.encode('utf-8')

    data_length = len(data)
    response_headers.append(('Content-Length', str(data_length)))
    return data, status, response_headers


def create(environ, start_response):
    data, status, response_headers = response_of(environ)
    start_response(status, response_headers)
    return iter([data])
