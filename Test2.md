# Test method

For this test, I've setup two machines.

- Client Machine
  This one runs both the database (PostgreSQL) and the rest clients. For each test, a new PostgreSQL container is initialized with the default data (
  loaded from [database.sql] (scripts/database.sql)).

- Server Machine
  A i7 laptop with Intel(R) Core(TM) i7-7700HQ (4 cores / 8 threads):
    - the scaphandre docker image on port
      8081: ```docker run --rm -ti -v /sys/class/powercap:/sys/class/powercap -v /proc:/proc -p 8081:8080 hubblo/scaphandre prometheus``` and this
      should be running at all times
    - the application itself

## The benchmark

For each application version, we do the following:

- start up a new database on machine (1)
- measure a baseline power usage, by taking the amount of joules that was used when the server was mostly idle - most of the services were disabled.
- start the application to be measured
- fire up the client(s)  with the following actions and measure the total latency:
    - list all the available products
    - select 5 random products and request their details
    - make a purchase by posting a new order with the selected products
    - check if the order was correctly put
- in other words, each request on the results table is actually made up of 8 physical http requets, of which only 1 actually makes a change to the
  database.
- the clients will run for exactly 2 minutes
- when all the clients are 'done', we measure the total energy used during the test and subtract the baseline energy measured earlier

# Results (1 client thread)

The full CSV with the results can be seen [here] (test2.csv)

| image        | samples | duration | avg_lat | median_lat | p99_lat | max_lat | energy(J) | avg_power(W) | energy_per_req |
|--------------|---------|----------|---------|------------|---------|---------|-----------|--------------|----------------|
| javaquarkus  | 5182    | 120      | 22.21   | 21         | 71      | 559     | 318.77    | 318.77       | 0.06           |
| ruby         | 1694    | 120      | 69.99   | 65         | 121     | 191     | 223.41    | 223.41       | 0.13           |
| golang       | 5140    | 120      | 22.42   | 21         | 70      | 77      | 248.86    | 248.86       | 0.05           |
| node         | 3461    | 120      | 33.80   | 28         | 88      | 119     | 182.89    | 182.89       | 0.05           |
| pythondjango | 568     | 120      | 210.58  | 205        | 279     | 467     | 507.56    | 507.56       | 0.89           |
| kotlin       | 5813    | 120      | 19.73   | 17         | 64      | 308     | 302.70    | 302.70       | 0.05           |
| scala        | 1753    | 120      | 67.58   | 58         | 286     | 1102    | 945.33    | 945.33       | 0.54           |
| rust         | 4978    | 120      | 23.01   | 20         | 74      | 1544    | 106.05    | 106.05       | 0.02           |
| pythonjude   | 4936    | 120      | 23.46   | 19         | 100     | 329     | 185.51    | 185.51       | 0.04           |
| pythonjudev2 | 5247    | 120      | 21.97   | 20         | 74      | 112     | 195.21    | 195.21       | 0.04           |

The 'worst' version in terms of energy usage on a per-request basis is the python version that uses django framework, but I think there might be
something wrong with it. The pythonjude version (a framework-less version written in python using gunicorn, psycopg and rapidjson) is better than most
so I don't think the django version reflects anything about the language, but maybe tell us something about using frameworks that do too much? In any
case, I will reach out to the author to see if we can improve things.

The scala version also produces some unexpected results, but it's probably due to my inability to use the stack properly. I will revisit that as well.

Other than those two ouliers, the energy consumption on a per-client basis is pretty much equivalent and, in my opinion, very low. Keep in mind that
each 'request' above is actually comprised of 8 actual http requests.

# Results (4 client threads)

| image        | samples | avg_lat | median_lat | p99_lat | max_lat | energy(J) | avg_power(W) | energy_per_req |
|--------------|---------|---------|------------|---------|---------|-----------|--------------|----------------|
| javaquarkus  | 19370   | 23.96   | 21         | 81      | 717     | 482.33    | 482.33       | 0.02           |
| ruby         | 1715    | 279.57  | 327        | 413     | 489     | 204.44    | 204.44       | 0.12           |
| golang       | 16949   | 27.35   | 25         | 84      | 221     | 332.64    | 332.64       | 0.02           |
| node         | 13196   | 35.35   | 31         | 97      | 682     | 516.11    | 516.11       | 0.04           |
| pythondjango | 2302    | 207.91  | 200        | 377     | 533     | 1574.73   | 1574.73      | 0.68           |
| kotlin       | 19701   | 23.03   | 21         | 74      | 1057    | 474.35    | 474.35       | 0.02           |
| scala        | 2052    | 235.66  | 53         | 205     | 43965   | 239.77    | 239.77       | 0.12           |
| rust         | 17941   | 25.61   | 22         | 77      | 212     | 225.37    | 225.37       | 0.01           |
| pythonjude   | 14990   | 31.07   | 22         | 101     | 137     | 447.92    | 447.92       | 0.03           |
| pythonjudev2 | 11351   | 41.48   | 28         | 114     | 515     | 325.35    | 325.35       | 0.03           |

Much better results for the scala version this time around, which says something about the scalability of that version, even with a very 'wrong'
implementation.
Python+django is still the worst one, but it saw an improvement on a per-request basis, like most implementations. Of note, the ruby version shows no
improvement in any number which suggests to me that it can't handle concurrent requests (maybe because
of [GIL](https://en.wikipedia.org/wiki/Global_interpreter_lock)). Regardless, it had good numbers to begin with.

The fact that most implementations saw an improvement on a per-request basis, suggests that maybe its more energy efficient to use more cores of a CPU
than more CPUs or more servers. While intuitively I think it makes sense, since the cores share much of the CPU infrastructure, if one core is busy,
all cores sharing the same power line will be running at high frequency but if they are idling, they will be just wasting that energy. It would be
interesting to repeat this test using a dual socket server-class machine to see if the results match of a consumer grade laptop.
However, I'm not sure how can we use this information in practice, specially if using public cloud providers - they are already sharing slices of CPU
per VM anyway.

# Observations

I've seen posts claiming that we should use X or Y language due to power usage, but I think this test paints a more realistic picture of a typical web
application nowadays. To put into perpective, the cloud carbon footprint
project [models 1MB of traffic as using about 3J](https://www.cloudcarbonfootprint.org/docs/methodology/#networking)
It's unreasonable to imagine that a user making these requests in the real world would transfer more than 1MB if we account for images, additional
javascript, css, http overhead, SSL overhead, etc. (Note to self: compute the total data transfered for this benchmark)
Also keep in mind that we're not accounting for the energy used by the database, which is likely to matter a lot.


