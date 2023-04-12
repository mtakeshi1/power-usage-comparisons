all: go quarkus nodejs pythonjude pythondjango rails benchmarkcode pgsql

go:
	docker build -t power/golang golang/simple-rest/

quarkus:
	+$(MAKE) -C java/quarkus-sample-rest

nodejs:
	docker build -t power/node node/simple-rest/

pythonjude:
	docker build -t power/jude python/jude/v1/

pythondjango:
	docker build -t power/django python/django_rest/

rails:
	docker build -t power/ruby ruby/simple-api/

pgsql:
	docker build -t power/pgsql scripts/

benchmarkcode:
	benchmark/gradlew build
