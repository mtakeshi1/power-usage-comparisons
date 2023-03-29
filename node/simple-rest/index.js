const express = require('express');
const bodyParser = require('body-parser');
const { Pool } = require('pg');
require('dotenv').config()

const app = express();
const port = process.env.PORT || 3000;
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

app.get('/products', async (req, res) => {
    try {
      const { rows } = await pool.query('SELECT * FROM product');
      res.send(rows);
    } catch (error) {
      console.error(error);
      res.sendStatus(500);
    }
  });
  
app.post('/products', async (req, res) => {
    const { name, description, price } = req.body;
    try {
      const { rows } = await pool.query(
        'INSERT INTO product (name, description, price) VALUES ($1, $2, $3) RETURNING *',
        [name, description, price]
      );
      res.send(rows[0]);
    } catch (error) {
      console.error(error);
    }
});

app.get('/', (req, res) => {
    res.send(`<h1>Bla</h1>`)
});

app.listen(port, () => {
    console.log(`Server listening on the port  ${port}`);
})
