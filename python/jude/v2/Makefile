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
	-e DB_CONNECTION_STRING=postgresql://myself:mysafepassword@THE-HOST:5432/mydatabase \
	-p 8000:8000 \
	--name takeshi-app \
	powerjudev2

test-scenario:
	rm -rf reports
	mkdir reports
	curl localhost:8000/products >> reports/all_products.txt
	curl localhost:8000/products/1 >> reports/product_1.txt
	curl localhost:8000/products/5 >> reports/product_5.txt
	curl -v localhost:8000/orders/new -X POST -d '[{"productId": 3, "amount": 9}, {"productId": 9, "amount": 3}]'
	curl -v localhost:8000/orders/new -X POST -d '[{"productId": 1, "amount": 4}, {"productId": 5, "amount": 2}]'
	curl -v localhost:8000/orders/new -X POST -d '[]'
	curl -v localhost:8000/orders/2 >> reports/order_2.txt
