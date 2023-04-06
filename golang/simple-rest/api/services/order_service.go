package services

import (
	"context"
	"database/sql"
	"log"
	db "simple-rest/api/db"
	"simple-rest/api/domain"
)

func CreateOrder(ctx context.Context, orderEntries []domain.OrderEntry) int {
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

func GetOrderById(ctx context.Context, cartId int) *domain.Order {
	cart := getCartById(cartId)
	entries := getOrderEntriesByCartId(cartId)
	return &domain.Order{
		ID:      cartId,
		Total:   cart.Total,
		Entries: entries,
	}
}

func createCart(ctx context.Context, tx *sql.Tx) (cart *domain.Cart) {
	sqlStatement := `INSERT INTO shoppingcart (total) VALUES ($1) RETURNING id`
	row := tx.QueryRowContext(ctx, sqlStatement, 0)

	var c domain.Cart
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

func getCartById(cartId int) (cart *domain.Cart) {
	db := db.GetDB()
	row := db.QueryRow("SELECT id, total FROM shoppingcart WHERE id=$1", cartId)
	var c domain.Cart
	err := row.Scan(&c.ID, &c.Total)
	if err != nil {
		log.Print(err)
	}
	return &c
}

func getOrderEntriesByCartId(cartId int) *[]domain.OrderEntry {
	db := db.GetDB()
	rows, err := db.Query("SELECT product_id, amount FROM productorder WHERE shoppingcart_id = $1", cartId)
	if err != nil {
		log.Print(err)
	}

	var orderEntries []domain.OrderEntry
	for rows.Next() {
		var o domain.OrderEntry
		rows.Scan(&o.ProductId, &o.Amount)
		orderEntries = append(orderEntries, o)
	}
	defer rows.Close()

	return &orderEntries
}

func createTransaction(ctx context.Context) *sql.Tx {
	db := db.GetDB()
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
