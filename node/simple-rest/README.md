```shell script
npm install
npm run start
```

Edit `.env`, default:
```
PORT=3000
PG_HOST=localhost
PG_PORT=51435
PG_DATABASE=quarkus
PG_USER=quarkus
PG_PWD=quarkus
```

- 
```
curl --request GET \
  --url http://localhost:3000/products


curl --request POST \
  --url http://localhost:3000/products \
  --header 'Content-Type: application/json' \
  --data '{
	"name": "test",
	"description": "bla bla bla",
	"price": 12.5
}'


curl --request GET \
  --url http://localhost:3000/products/1


curl --request POST \
  --url http://localhost:3000/orders/new \
  --header 'Content-Type: application/json' \
  --data '[
	{
		"productId": 1,
		"amount": 10
	},
	{
		"productId": 2,
		"amount": 15
	}
]'


curl --request GET \
  --url http://localhost:3000/orders/1


```