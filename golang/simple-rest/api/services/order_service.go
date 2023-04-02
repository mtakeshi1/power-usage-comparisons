package services

import (
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

func CreateOrder(orderEntries []OrderEntry) int {
	cart := createCart()
	total := 0.0
	for _, orderEntry := range orderEntries {
		product := GetProductById(orderEntry.ProductId)
		createProductOrder(product.ID, orderEntry.Amount, cart.ID)
		total += orderEntry.Amount * product.Price
	}
	updateCart(cart.ID, total)
	return cart.ID
}

func GetOrderById(cartId int) *Order {
	cart := getCartById(cartId)
	entries := getOrderEntriesByCartId(cartId)
	return &Order{
		ID:      cartId,
		Total:   cart.Total,
		Entries: entries,
	}
}

func createCart() (cart *Cart) {
	db := db.NewDbConnector().OpenDBConnection()
	sqlStatement := `INSERT INTO shoppingcart (total) VALUES ($1) RETURNING id`
	row := db.QueryRow(sqlStatement, 0)
	defer db.Close()
	var c Cart
	err := row.Scan(&c.ID)
	if err != nil {
		log.Print(err)
	}
	return &c
}

func createProductOrder(productId int, amount float64, cartId int) {
	db := db.NewDbConnector().OpenDBConnection()
	sqlStatement := `INSERT INTO productorder (product_id, amount, shoppingcart_id) VALUES ($1, $2, $3)`
	_, err := db.Exec(sqlStatement, productId, amount, cartId)
	defer db.Close()
	if err != nil {
		log.Print(err)
	}
}

func updateCart(id int, total float64) {
	db := db.NewDbConnector().OpenDBConnection()
	sqlStatement := `UPDATE shoppingcart SET total = $2 where id = ($1)`
	_, err := db.Exec(sqlStatement, id, total)
	defer db.Close()
	if err != nil {
		log.Print(err)
	}
}

func getCartById(cartId int) (cart *Cart) {
	db := db.NewDbConnector().OpenDBConnection()
	row := db.QueryRow("SELECT id, total FROM shoppingcart WHERE id=$1", cartId)
	defer db.Close()
	var c Cart
	err := row.Scan(&c.ID, &c.Total)
	if err != nil {
		log.Print(err)
	}
	return &c
}

func getOrderEntriesByCartId(cartId int) *[]OrderEntry {
	db := db.NewDbConnector().OpenDBConnection()
	rows, err := db.Query("SELECT product_id, amount FROM productorder WHERE shoppingcart_id = $1", cartId)
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
	defer db.Close()

	return &orderEntries
}
