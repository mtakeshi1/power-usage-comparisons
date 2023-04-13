# Python - Jude - V1

Python implementation, using raw materials.

My will behind this stack, is to compare raw WSGI application that are essentially single-threaded.

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

## Use of better data structures?

Some of the computations (especially merging collected information) are written in plain Python.
There could be a room for improvements here, by using constructions that would allow faster access
and operations (like Pandas or NumPy). In particular, open issues in Psycopg are about getting results
as NumPy objects. This can be more than interesting to work on.

## JIT or compilation

It is currently impossible to have Numba JIT or AOT compilation in Python 3.11.

We have not explored the benefit of Cython, but maybe the routing part could be optimised further.

