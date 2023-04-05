package services

import (
	"context"
	"database/sql"
	"log"
	db "simple-rest/api"
)

type OrderEntry struct {
	ProductId int     `json:"productId"`
	Amount    float64 `json:"amount"`
}

type Order struct {
	ID      int           `json:"id"`
	Total   float64       `json:"total"`
	Entries *[]OrderEntry `json:"entries"`
}

type Cart struct {
	ID    int     `json:"id"`
	Total float64 `json:"total"`
}

type ProductOrder struct {
	ID             int `json:"id"`
	Amount         int `json:"amount"`
	ProductId      int `json:"productId"`
	ShoppingcartId int `json:"shoppingcartId"`
}

func CreateOrder(ctx context.Context, orderEntries []OrderEntry) int {
	tx := createTransaction(ctx)
	defer tx.Rollback()

	cart := createCart(ctx, tx)
	total := 0.0
	for _, orderEntry := range orderEntries {
		product := GetProductByIdWithTx(ctx, tx, orderEntry.ProductId)
		createProductOrder(ctx, tx, product.ID, orderEntry.Amount, cart.ID)
		total += orderEntry.Amount * product.Price
	}
	updateCart(ctx, tx, cart.ID, total)
	commitTransaction(tx)
	return cart.ID
}

func GetOrderById(ctx context.Context, cartId int) *Order {
	tx := createTransaction(ctx)
	defer tx.Rollback()

	cart := getCartById(ctx, tx, cartId)
	entries := getOrderEntriesByCartId(ctx, tx, cartId)
	return &Order{
		ID:      cartId,
		Total:   cart.Total,
		Entries: entries,
	}
}

func createCart(ctx context.Context, tx *sql.Tx) (cart *Cart) {
	sqlStatement := `INSERT INTO shoppingcart (total) VALUES ($1) RETURNING id`
	row := tx.QueryRowContext(ctx, sqlStatement, 0)

	var c Cart
	err := row.Scan(&c.ID)
	if err != nil {
		log.Print(err)
	}
	return &c
}

func createProductOrder(ctx context.Context, tx *sql.Tx, productId int, amount float64, cartId int) {
	sqlStatement := `INSERT INTO productorder (product_id, amount, shoppingcart_id) VALUES ($1, $2, $3)`
	_, err := tx.ExecContext(ctx, sqlStatement, productId, amount, cartId)
	if err != nil {
		log.Print(err)
	}
}

func updateCart(ctx context.Context, tx *sql.Tx, id int, total float64) {
	sqlStatement := `UPDATE shoppingcart SET total = $2 where id = ($1)`
	_, err := tx.ExecContext(ctx, sqlStatement, id, total)
	if err != nil {
		log.Print(err)
	}
}

func getCartById(ctx context.Context, tx *sql.Tx, cartId int) (cart *Cart) {
	row := tx.QueryRowContext(ctx, "SELECT id, total FROM shoppingcart WHERE id=$1", cartId)
	var c Cart
	err := row.Scan(&c.ID, &c.Total)
	if err != nil {
		log.Print(err)
	}
	return &c
}

func getOrderEntriesByCartId(ctx context.Context, tx *sql.Tx, cartId int) *[]OrderEntry {
	rows, err := tx.QueryContext(ctx, "SELECT product_id, amount FROM productorder WHERE shoppingcart_id = $1", cartId)
	if err != nil {
		log.Print(err)
	}

	var orderEntries []OrderEntry
	for rows.Next() {
		var o OrderEntry
		rows.Scan(&o.ProductId, &o.Amount)
		orderEntries = append(orderEntries, o)
	}
	defer rows.Close()

	return &orderEntries
}

func createTransaction(ctx context.Context) *sql.Tx {
	db := db.GetDbConnector().OpenDBConnection()
	tx, err := db.BeginTx(ctx, nil)
	if err != nil {
		log.Print(err)
		return nil
	}
	return tx
}

func commitTransaction(tx *sql.Tx) {
	if err := tx.Commit(); err != nil {
		log.Print(err)
	}
}
