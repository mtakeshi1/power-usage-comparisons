- starting the database:

from the root folder, run this:

```
docker run --rm -e POSTGRES_USER=myself -e POSTGRES_PASSWORD=mysafepassword -e POSTGRES_DB=mydatabase -p 5432:5432 -v $PWD/scripts:/docker-entrypoint-initdb.d/ --name pgsql postgres:14.7
```

nodes for myself:

try this to plot the graph
https://pypi.org/project/plotext/

