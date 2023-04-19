# Python - Jude - V2

Python implementation, using raw materials.

My will behind this stack, is to compare raw WSGI application that are essentially single-threaded.

The stack uses GUnicorn directly, as a translator between HTTP and WSGI.
It uses Psycopg as connector to the database.
As far as possible, I have followed the recommendations of the
respective documentations.

## New in V2:

More logic has been split in standalone pure methods.
Since invoking methods might bring an overhead in Python, we
create a V2 with this. In a future work, we plan to integrate
Numba.

## References

https://peps.python.org/pep-0333/

https://docs.gunicorn.org/en/latest/settings.html

https://www.psycopg.org/psycopg3/docs/api/connections.html

## Build Docker images

In root folder:
```
docker build -t powerjudev2 .
```

Running the image assumes you have a valid available
PostgreSQL instance. You provide the connection string
as an environment variable:

```
docker run --rm \
-e DB_CONNECTION_STRING=postgresql://myself:mysafepassword@host:5432/mydatabase \
-p 8000:8000 \
--name takeshi-app \
powerjudev2
```

You can use the shortcuts in the `Makefile`.
Check the thing is working:

```
make test-schenario
```

# Further work

## Use of better data structures?

Some computations (especially merging collected information) are written in plain Python.
There could be a room for improvements here, by using constructions that would allow faster access
and operations (like Pandas or NumPy). In particular, open issues in Psycopg are about getting results
as NumPy objects. This can be more than interesting to work on.

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

## Asynchronous handling

According to GUnicorn documentation, the use of `sync` worker mode
is highly discouraged when there is no reverse proxy in front of the application.
In a real world scenario, we should either modify the worker class,
or modify the architecture.

It has to be noted that current state of `psycopg3` does not allow anymore interplay
with `gevent` and `eventlet`.

Another completely different approach, could be to rely on a ASGI framework
like `Starlette`. Wehave not investigated this possibility.

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

