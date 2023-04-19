import dataclasses
import tempfile
import typing

import asyncio
import io
import psycopg_pool
import psycopg
from psycopg.rows import dict_row
import rapidjson
import os

# --- APPLICATION GLOBAL CONTEXT ----------------------------------------------
connection_string = os.getenv("DB_CONNECTION_STRING")
pool = psycopg_pool.AsyncConnectionPool(connection_string, open=False, max_size=5)

# --- APPLICATION GLOBAL PROTOCOLS --------------------------------------------
HEADERS_LIST = typing.List[typing.Tuple[bytes, bytes]]
ASGI_ENVIRON = typing.Dict[str, typing.Any]
ASGI_RECEIVE = typing.Any
CONNECTION_POOL = psycopg_pool.ConnectionPool

HTTP_ENTITY_ANSWER = typing.Tuple[str, typing.Literal[200, 201], HEADERS_LIST]


class HTTP_ENTITY(typing.Protocol):
    async def __call__(self, scope: ASGI_ENVIRON, receive: ASGI_RECEIVE, pool: CONNECTION_POOL) -> HTTP_ENTITY_ANSWER:
        ...


async def _read_body(receive) -> io.BytesIO:
    body = b''
    while True:
        message = await receive()
        body += message.get('body', b'')
        if not message.get('more_body', False):
            break

    return io.BytesIO(body)


# --- HTTP ENTITIES -----------------------------------------------------------
@dataclasses.dataclass(frozen=True)
class NewOrder:
    async def __call__(self, scope: ASGI_ENVIRON, receive: ASGI_RECEIVE, pool: CONNECTION_POOL) -> HTTP_ENTITY_ANSWER:
        product_records, body_bytes = await asyncio.gather(
            NewOrder.get_product_records_from_database(),
            _read_body(receive)
        )

        entry_records = rapidjson.load(body_bytes)
        total = NewOrder.total_of_entries(product_records, entry_records)

        async with pool.connection() as connection:
            async with connection.pipeline():
                async with connection.cursor() as cursor:
                    await cursor.execute(
                        "INSERT INTO public.shoppingcart (total) VALUES (%s) RETURNING id",
                        params=[total]
                    )
                    order_id, = await cursor.fetchone()

                    await cursor.executemany(
                        "INSERT INTO public.productorder (shoppingcart_id, product_id, amount) VALUES (%s,%s,%s)",
                        params_seq=list(
                            (order_id, entry['productId'], entry['amount'])
                            for entry in entry_records
                        )
                    )

        return rapidjson.dumps(order_id).encode('utf-8'), 201, [(b'Content-Type', b'application/json')]

    @staticmethod
    async def get_product_records_from_database():
        async with pool.connection() as connection:
            await connection.set_autocommit(True)
            async with connection.cursor() as cursor:
                await cursor.execute(
                    "SELECT id, price FROM public.product "
                )
                return await cursor.fetchall()

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

    async def __call__(self, scope: ASGI_ENVIRON, receive: ASGI_RECEIVE, pool: CONNECTION_POOL) -> HTTP_ENTITY_ANSWER:
        id = int(self.id)
        async with pool.connection() as connection:
            await connection.set_autocommit(True)
            async with connection.cursor(row_factory=dict_row) as cursor:
                await cursor.execute(
                    "SELECT id, name, price, description FROM public.product where id=%s",
                    params=[id]
                )
                product = await cursor.fetchone()

        return rapidjson.dumps(product).encode('utf-8'), 200, [(b'Content-Type', b'application/json')]


async def view_product_information(scope: ASGI_ENVIRON, receive: ASGI_RECEIVE, protocol: CONNECTION_POOL) -> HTTP_ENTITY_ANSWER:
    async with pool.connection() as connection:
        await connection.set_autocommit(True)
        async with connection.cursor(row_factory=dict_row) as cursor:
            await cursor.execute(
                "SELECT id, name FROM public.product "
            )
            products = await cursor.fetchall()

    return rapidjson.dumps(products).encode('utf-8'), 200, [(b'Content-Type', b'application/json')]


@dataclasses.dataclass(frozen=True)
class ViewOrder:
    id: bytes

    async def __call__(self, scope: ASGI_ENVIRON, receive: ASGI_RECEIVE, pool: CONNECTION_POOL) -> HTTP_ENTITY_ANSWER:
        id = int(self.id)
        async with pool.connection() as connection:
            await connection.set_autocommit(True)
            async with connection.pipeline():
                async with connection.cursor() as cursor:
                    await cursor.execute(
                        "SELECT total FROM public.shoppingcart WHERE id=%s",
                        params=[id]
                    )
                    await cursor.execute(
                        "SELECT amount, product_id from public.productorder where shoppingcart_id=%s",
                        params=[id]
                    )

                    total, = await cursor.fetchone()
                    cursor.nextset()
                    order_entries = await cursor.fetchall()

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
        )).encode('utf-8'), 200, [(b'Content-Type', b'application/json')]


# --- APPLICATION -------------------------------------------------------------
def entity_of(scope: ASGI_ENVIRON) -> typing.Optional[HTTP_ENTITY]:
    url = str(scope.get('path')).encode('ascii')
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
    return None


pool_opened = False
async def create(scope, receive, send):
    global pool_opened
    assert scope['type'] == 'http'

    response_headers = None; data = None
    try:
        entity = entity_of(scope)
        if entity is None:
            data = b''; status = 404; response_headers = None
        else:
            if not pool_opened:
                await pool.open(wait=True)
                pool_opened = True
            data, status, response_headers = await entity(scope, receive, pool)
        del entity
    except psycopg.Error:
        import traceback
        data = str(traceback.format_exc()).encode('utf-8')
        status = 500
    except Exception:
        import traceback
        data = str(traceback.format_exc()).encode('utf-8')
        status = 400

    await send(dict(
        type="http.response.start",
        status=status,
        headers=response_headers or list()
    ))
    await send(dict(
        type="http.response.body",
        body=data or b''
    ))
