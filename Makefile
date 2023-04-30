all: go quarkus nodejs pythondjango rails benchmarkcode pgsql kot rustaxum pythonjudev2 pythonjudev3 scalaio

go:
	docker build -t power/golang golang/simple-rest/

quarkus:
	+$(MAKE) -C java/quarkus-sample-rest

nodejs:
	docker build -t power/node node/simple-rest/

pythonjudev2:
	docker build -t power/judev2 python/jude/v2/

pythonjudev3:
	docker build -t power/judev3 python/jude/v3/

pythondjango:
	docker build -t power/django python/django_rest/

rails:
	docker build -t power/ruby ruby/simple-api/

pgsql:
	docker build -t power/pgsql scripts/

benchmarkcode:
	+$(MAKE) -C benchmark

kot:
	+$(MAKE) -C kotlin/simple-api

rustaxum:
	docker build -t power/rust rust/axum_diesel/

scalaio:
	+$(MAKE) -C scala/scala-rest