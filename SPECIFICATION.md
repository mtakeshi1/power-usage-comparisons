Minimum things to implement.

# Database Tables:

```sql
CREATE TABLE public.product (
    id integer NOT NULL,
    description character varying(255),
    name character varying(255),
    price double precision NOT NULL
);
CREATE TABLE public.productorder (
    id integer NOT NULL,
    amount integer NOT NULL,
    product_id integer,
    shoppingcart_id integer
);
CREATE TABLE public.shoppingcart (
    id integer NOT NULL,
    total double precision NOT NULL
);
```

# REST endpoints:

### GET /products
```json
[{"id":1,"name":"The Grandfather"},{"id":2,"name":"Shadow Dancer"}]
```

### GET /products/{id}
```json
{
  "id": 1,
  "name": "The Grandfather",
  "description": "Two-Hand Damage: 250-290 Required Strength: 189 +3 to all skills 20% Chance to cast level 18 Tornado on striking",
  "price": 1500
}
```

### POST /orders/new
body: 
```json
[{"productId":1, "amount":1}]
```

Example with curl:
```shell
curl -H 'Content-Type: application/json' -X POST -d'[{"productId":1, "amount":1}]'  http://localhost:8080/orders/new
```

### GET /orders/{id}
```json
{
  "id": 1,
  "entries": [
    {
      "productId": 1,
      "amount": 1
    }
  ],
  "total": 1500
}
```