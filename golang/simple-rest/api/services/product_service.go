package services

import (
	"context"
	"database/sql"
	"log"
	"simple-rest/api/db"
	"simple-rest/api/domain"
)

func GetAllProducts() *[]domain.Product {
	db := db.GetDB()
	rows, err := db.Query("SELECT id, name, description, price FROM product")
	if err != nil {
		log.Print(err)
	}

	var products []domain.Product
	for rows.Next() {
		var p domain.Product
		rows.Scan(&p.ID, &p.Name, &p.Description, &p.Price)
		products = append(products, p)
	}
	defer rows.Close()

	return &products
}

func CreateProduct(p *domain.Product) (product *domain.Product, err error) {
	db := db.GetDB()
	sqlStatement := `INSERT INTO product (name, description, price) VALUES ($1, $2, $3)`
	_, errDB := db.Exec(sqlStatement, p.Name, p.Description, p.Price)
	if errDB != nil {
		return nil, errDB
	}
	return p, nil
}

func GetProductById(productId int) (product *domain.Product) {
	db := db.GetDB()
	row := db.QueryRow("SELECT id, name, description, price FROM product WHERE id=$1", productId)
	var p domain.Product
	err := row.Scan(&p.ID, &p.Name, &p.Description, &p.Price)
	if err != nil {
		log.Print(err)
	}
	return &p
}

func GetProductByIdWithTx(ctx context.Context, tx *sql.Tx, productId int) (product *domain.Product) {
	row := tx.QueryRowContext(ctx, "SELECT id, name, description, price FROM product WHERE id=$1", productId)
	var p domain.Product
	err := row.Scan(&p.ID, &p.Name, &p.Description, &p.Price)
	if err != nil {
		log.Print(err)
	}
	return &p
}
