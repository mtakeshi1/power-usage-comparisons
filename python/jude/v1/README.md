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

Some computations (especially merging collected information) are written in plain Python.
There could be a room for improvements here, by using constructions that would allow faster access
and operations (like Pandas or NumPy). In particular, open issues in Psycopg are about getting results
as NumPy objects. This can be more than interesting to work on.

## JIT or compilation

It is currently impossible to have Numba JIT or AOT compilation in Python 3.11.

We have not explored the benefit of Cython, but maybe the routing part could be optimised further,
as it is mainly a branching on strings that follow a specific protocol. An approach could
be to replace all occurrences of `/` by the `NUL` C-character. This can allow
C-like iteration on every segment of the string. It would then be sufficient to
return the Python object and exit Cython realm.

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

