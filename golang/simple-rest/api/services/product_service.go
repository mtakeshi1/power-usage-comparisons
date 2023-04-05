package services

import (
	"context"
	"database/sql"
	"log"
	db "simple-rest/api"
)

type Product struct {
	ID          int     `json:"id"`
	Name        string  `json:"name"`
	Description string  `json:"description"`
	Price       float64 `json:"price"`
}

func GetAllProducts() *[]Product {
	db := db.GetDbConnector().OpenDBConnection()
	rows, err := db.Query("SELECT id, name, description, price FROM product")
	if err != nil {
		log.Print(err)
	}

	var products []Product
	for rows.Next() {
		var p Product
		rows.Scan(&p.ID, &p.Name, &p.Description, &p.Price)
		products = append(products, p)
	}

	defer rows.Close()
	defer db.Close()

	return &products
}

func CreateProduct(p *Product) (product *Product, err error) {
	db := db.GetDbConnector().OpenDBConnection()
	sqlStatement := `INSERT INTO product (name, description, price) VALUES ($1, $2, $3)`
	_, errDB := db.Exec(sqlStatement, p.Name, p.Description, p.Price)
	defer db.Close()
	if errDB != nil {
		return nil, errDB
	}
	return p, nil
}

func GetProductById(productId int) (product *Product) {
	db := db.GetDbConnector().OpenDBConnection()
	row := db.QueryRow("SELECT id, name, description, price FROM product WHERE id=$1", productId)
	defer db.Close()
	var p Product
	err := row.Scan(&p.ID, &p.Name, &p.Description, &p.Price)
	if err != nil {
		log.Print(err)
	}
	return &p
}

func GetProductByIdWithTx(ctx context.Context, tx *sql.Tx, productId int) (product *Product) {
	row := tx.QueryRowContext(ctx, "SELECT id, name, description, price FROM product WHERE id=$1", productId)
	var p Product
	err := row.Scan(&p.ID, &p.Name, &p.Description, &p.Price)
	if err != nil {
		log.Print(err)
	}
	return &p
}
