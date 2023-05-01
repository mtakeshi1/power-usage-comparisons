db:
	docker run --rm \
	-e POSTGRES_USER=myself \
	-e POSTGRES_PASSWORD=mysafepassword \
	-e POSTGRES_DB=mydatabase \
	-p 5432:5432 \
	-v ${PWD}/scripts:/docker-entrypoint-initdb.d/ \
	--name pgsql postgres:14.7

serve:
	docker run --rm \
	-e DB_CONNECTION_STRING=postgresql://myself:mysafepassword@192.168.0.26:5432/mydatabase \
	-v ${PWD}:/usr/app \
	-p 8000:8000 \
	powerjudev4

test-javaclient:
	java RequestMaker.java

test-scenario:
	rm -rf reports
	mkdir reports
	curl localhost:8000/products >> reports/all_products.txt
	curl localhost:8000/products/1 localhost:8000/products/5 >> reports/products.txt
	curl -v localhost:8000/orders/new -X POST -d '[{"productId": 3, "amount": 9}, {"productId": 9, "amount": 3}]'
	curl -v localhost:8000/orders/new -X POST -d '[{"productId": 1, "amount": 4}, {"productId": 5, "amount": 2}]' >> reports/post_report.txt
	curl -v localhost:8000/orders/new -X POST -d '[]'
	curl -v localhost:8000/orders/2 >> reports/orders.txt
	curl -v localhost:8000/orders/2 localhost:8000/products localhost:8000/products/1 localhost:8000/products/5