# Python - Jude - V3

Python implementation, using raw materials.

The stack uses GUnicorn directly with Uvicorn worker class (async),
as a translator between HTTP and ASGI.
It uses Psycopg as connector to the database.
As far as possible, I have followed the recommendations of the
respective documentations.

## New in V3:

We have used Asynchronous paradigms to compare with the synchronous
case from previous V2 implementation. We managed to successfully translate
most of the patterns, except `COPY` feature of PostgreSQL used in
`/orders/new`. For this route, we have used a pipeline with many `INSERT`
statements. We still have to figure out how to efficiently copy to the
database without hooking too much in the loop.

## References

https://peps.python.org/pep-0333

https://asgi.readthedocs.io/_/downloads/en/latest/pdf/

https://docs.gunicorn.org

https://www.uvicorn.org

https://www.psycopg.org

## Build Docker images

In root folder:
```
docker build -t powerjudev3 .
```

Running the image assumes you have a valid available
PostgreSQL instance. You provide the connection string
as an environment variable:

```
docker run --rm \
-e DB_CONNECTION_STRING=postgresql://myself:mysafepassword@host:5432/mydatabase \
-p 8000:8000 \
--name takeshi-app \
powerjudev3
```

You can use the shortcuts in the `Makefile`.
Check the thing is working:

```
make test-schenario
```

# Further work

## JIT or compilation

It is currently impossible to have Numba JIT or AOT compilation in Python 3.11
(see https://numba.readthedocs.io/en/stable/user/installing.html and Github issue
https://github.com/numba/numba/issues/8304).

See also (https://docs.python.org/3/whatsnew/3.11.html and references therein,
for a detailed view about what is new in CPython 3.11 support).

## Stronger interplay with JSON

The implementation now uses RapidJSON (https://rapidjson.org/index.html)
as JSON library. We use the library by using the standard Python's wrapper of it
(see https://pypi.org/project/python-rapidjson/). We have not investigated the
gain we could have in working directly in Cython for our use cases, without
relying on a generic wrapper library.

# A word on the paradigm

The paradigm used to organise the application does not follow conventional RestAPI.
The closest approach in literature we have found, are known as
**Transactional Scripts**
(see for example https://martinfowler.com/eaaCatalog/transactionScript.html).

The main idea is to use types and classes for the following reasons only:

- The abstraction engages an algebraic structure (for example: it is a monoid)
- The abstraction encapsulates a mutable state it protects (for example: a connection pool)

Counter-example of this approach could be, for example, a `Product` type that
represents the aggregated information to be "a product". We consider those abstractions
as taxonomies, and we believe they do not resist change, nor unlock efficient
approach to problem.

Every route already stands for an HTTP Entity
(see https://www.w3.org/Protocols/rfc2616/rfc2616-sec7.html), and as such, is
responsible for its own concerns. Most of the external protocols
(SQL, WSGI, OpenAPI, are thought in the Clojure perspective "just use map").
Types are reserved for Functional Algebras and Object Oriented programming.

