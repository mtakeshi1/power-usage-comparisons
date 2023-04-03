# Python - Jude - V1

Python implementation, using raw materials.

My will behind this stack, is tocompare raw WSGI application that are essentially single-threaded.
The WSGI protocol is the de facto standard protocol for Python web applications, and compared to Java equivalent standard API.
It is an extension of the well-known and older CGI protocol.

Having process/worker based archictecture is common in the Pyton ecosystem, and delivers different
advantages like simplicity (you don't have to wonder about thread-safety, especially when using connection pools; you don't
have to wonder about context-switch and cache flush, ...) but also comes with a bunch of limitations
(for IO operations, it might not be the most efficient).

The stack uses GUnicorn directly, as a translator between HTTP and WSGI.
It uses Psycopg as connector to the database.
As far as possible, I have followed the recommendations of the
respective documentations.

## References

https://peps.python.org/pep-0333/

https://docs.gunicorn.org/en/latest/settings.html

https://www.psycopg.org/psycopg3/docs/api/connections.html

## Build Docker images

In root folder:
```
docker build -t powerjudev1 .
```

Running the image assumes you have a valid available
PostgreSQL instance. You provide the connection string
as an environment variable:

```
docker run --rm \
-e DB_CONNECTION_STRING=postgresql://myself:mysafepassword@host:5432/mydatabase \
-v ${PWD}:/usr/app \
-p 8000:8000 \
--name takeshi-app \
powerjudev1
```

You can use the shortcuts in the `Makefile`.
Check the thing is working:

```
make test-schenario
```

# Further work

## Pipeline mode

The Docker image we are based on, only support `libpq` library version 13.9.
Therefore, we cannot make use of Postgres **pipeline** mode. Having the mode activated could
reduce some of the operations in the code.

## Install is not production ready

The author of `psycopg` insists that for a production-ready release, we should build the
library ourselves. We did not.

Similarly, `simplejson` might be misused.

## Use of better data structures?

Some of the computations (especially merging collected information) are written in plain Python.
There could be a room for improvements here, by using constructions that would allow faster access
and operations (like Pandas or NumPy). In particular, open issues in Psycopg are about getting results
as NumPy objects. This can be more than interesting to work on.

## JIT or compilation

Another possible direction toward optimisation, could be to integrate a JIT like Numba,
or else an AOT compiler (still Numba) or another compilation technique like Cython.

This could be achieved very locally, on the router part for example. It does not have to
affect all the implementations.

