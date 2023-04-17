const express = require('express');
const bodyParser = require('body-parser');
const { Pool } = require('pg');
require('dotenv').config()

const createProductService = require('./api/service/product.service');
const createOrderService = require('./api/service/order.service');

const app = express();
const port = 8080;
app.use(bodyParser.json());

const pgHost = process.env.PG_HOST || "localhost";
const pgPort = process.env.PG_PORT || 51435;
const pgDatabase = process.env.PG_DATABASE || "quarkus";
const pgUser = process.env.PG_USER || "quarkus";
const pgPwd = process.env.PG_PWD || "quarkus";
const pool = new Pool({
  host: pgHost,
  port: pgPort,
  database: pgDatabase,
  user: pgUser,
  password: pgPwd,
});

const productService = createProductService(pool);
const orderService = createOrderService(pool);

app.get('/products', async (req, res) => {
  try {
    res.send(await productService.getAllProducts());
  } catch (error) {
    console.error(error);
    res.sendStatus(500);
  }
});

app.post('/products', async (req, res) => {
  const { name, description, price } = req.body;
  try {
    const product = await productService.createProduct(name, description, price);
    res.status(201).send(product);
  } catch (error) {
    console.error(error);
    res.sendStatus(500);
  }
});

app.get('/products/:id', async (req, res) => {
  try {
    const product = await productService.getProductById(req.params.id);
    res.send(product);
  } catch (error) {
    console.error(error);
    res.sendStatus(500);
  }
});

app.post('/orders/new', async (req, res) => {
  const orderEntries = req.body;
  try {
    const cartId = await orderService.createOrder(orderEntries);
    res.status(201).send("" + cartId);
  } catch (error) {
    console.error(error);
    res.sendStatus(500);
  }
});

app.get('/orders/:id', async (req, res) => {
  try {
    const order = await orderService.getOrderById(req.params.id);
    res.send(order);
  } catch (error) {
    console.error(error);
    res.sendStatus(500);
  }
});

app.get('/', (req, res) => {
  res.send(`<h1>Bla</h1>`)
});

app.listen(port, () => {
  console.log(`Server listening on the port  ${port}`);
})
