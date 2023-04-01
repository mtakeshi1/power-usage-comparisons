import dataclasses

import psycopg_pool
import simplejson
import os

# --- APPLICATION CONTEXT -----------------------------------------------------
connection_string = os.getenv("DB_CONNECTION_STRING")
pool = psycopg_pool.ConnectionPool(connection_string)

# --- HTTP WEB RESOURCE -------------------------------------------------------
status_map = {
    200: "OK",
    201: "Created",
    400: "Bad Request",
    404: "Not Found",
    500: "Internal Server Error"
}


def not_found(environ, headers):
    return None, 404


def new_order(environ, headers):
    entries = simplejson.load(environ.get('wsgi.input'))
    entries = list(
        (int(entry['productId']), int(entry['amount']))
        for entry in entries
    )

    with pool.connection() as connection:
        with connection.cursor() as cursor:
            cursor.execute(
                "SELECT "
                "id, name, price, description "
                "FROM public.product "
            )
            products = cursor.fetchall()

    products = {
        product_information[0]: dict(
            name=product_information[1],
            price=product_information[2],
            description=product_information[3]
        )
        for product_information in products
    }

    total = 0.
    for entry_id, entry_amount in entries:
        if entry_id not in products:
            raise ValueError
        else:
            product_price = products[entry_id].get('price')
            total += product_price*entry_amount

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
            entries = list(
                [order_id, product_id, amount]
                for product_id, amount in entries
            )
            cursor.executemany(
                "INSERT INTO public.productorder "
                "(shoppingcart_id, product_id, amount) "
                "values "
                "(%s, %s, %s)",
                params_seq=entries
            )

    headers.append(('Content-Type', 'application/json'))
    return simplejson.dumps(order_id), 201


@dataclasses.dataclass(frozen=True)
class ViewProduct:
    id: str

    def __call__(self, environ, headers):
        id = int(self.id)
        with pool.connection() as connection:
            with connection.cursor() as cursor:
                cursor.execute(
                    "SELECT "
                    "name, price, description "
                    "FROM public.product "
                    "where id=%s",
                    params=[id]
                )
                product = cursor.fetchone()

        product = dict(
            id=id,
            name=product[0],
            price=product[1],
            description=product[2]
        )
        headers.append(('Content-Type', 'application/json'))
        return simplejson.dumps(product), 200


def view_product_information(environ, headers, **kwargs):
    with pool.connection() as connection:
        with connection.cursor() as cursor:
            cursor.execute(
                "SELECT "
                "id, name "
                "FROM public.product "
            )
            products = cursor.fetchall()

    products = list(
        dict(
            id=product[0],
            name=product[1]
        )
        for product in products
    )
    headers.append(('Content-Type', 'application/json'))
    return simplejson.dumps(products), 200


@dataclasses.dataclass(frozen=True)
class ViewOrder:
    id: str

    def __call__(self, environ, headers):
        id = int(self.id)
        with pool.connection() as connection:
            with connection.cursor() as cursor:
                cursor.execute(
                    "SELECT "
                    "total "
                    "FROM public.shoppingcart "
                    "WHERE id=%s",
                    params=[id]
                )
                total, = cursor.fetchone()
                cursor.execute(
                    "SELECT "
                    "amount, product_id "
                    "from public.productorder "
                    "where shoppingcart_id=%s",
                    params=[id]
                )
                order_entries = cursor.fetchall()

        headers.append(('Content-Type', 'application/json'))
        return simplejson.dumps(dict(
            id=id,
            total=total,
            items=list(
                dict(
                    productId=entry[1],
                    amount=entry[0]
                )
                for entry in order_entries
            )
        )), 200


# --- APPLICATION -------------------------------------------------------------
def resource_of(segments):
    first, *segments = segments
    if first == 'products':
        if segments:
            first, *segments = segments
            return ViewProduct(id=first)
        else:
            return view_product_information
    elif first == 'orders' and segments:
        first, *segments = segments
        if first == 'new':
            return new_order
        else:
            return ViewOrder(id=first)

    return not_found


def create(environ, start_response):
    response_headers = list()
    try:
        url = environ.get('PATH_INFO')
        segments = url.split('/')
        segments = (segment for segment in segments if segment != '')
        segments = [segment for segment, _ in zip(segments, range(5))]
        resource = resource_of(segments)

        data, status = resource(environ, response_headers)
    except ValueError as e:
        data = str(e)
        status = 400
    except Exception as e:
        data = str(e)
        status = 500

    status = "{} {}".format(status, status_map[status])
    if data is None:
        data = b''
    else:
        data = data.encode('utf-8')
    data_length = len(data)
    response_headers.append(('Content-Length', str(data_length)))
    start_response(status, response_headers)
    return iter([data])
