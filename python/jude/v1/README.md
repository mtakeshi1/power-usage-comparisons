# Python - Jude - V1

Python implementation, using raw GUnicorn and Psycopg 3.

As far as possible, I have followed the recommendations of the
respective documentations, without pushing too far the
asynchronous handling in Psycopg.

Some scenario could have been optimized further, using the
provided `AsyncConnectionPool` and the `Pipe` mechanism.
I have not used them.

## References

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

Possible improvements are:

1. Make use of `AsyncConnectionPool` from `psycopg3` and/or pipes
2. Create proper builds for `psycopg` and `simplejson`, as both have C-build available
3. Use `pandas` for everything that looks like a computation
4. Make use of a proxy (`nginx`) for NIO connections

