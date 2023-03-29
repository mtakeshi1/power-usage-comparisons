package main

import (
	"database/sql"
	"fmt"
	"log"
	"net/http"

	"github.com/gin-gonic/gin"
	_ "github.com/lib/pq"
)

const (
	appHost = "localhost"
	appPort = 8081

	pgHost     = "localhost"
	pgPort     = 53146
	pgUser     = "quarkus"
	pgPwd      = "quarkus"
	pgDatabase = "quarkus"
)

type Product struct {
	ID           int     `json:"id"`
	Name         string  `json:"name"`
	Description  string  `json:"description"`
	Price        float32 `json:"price"`
}

func OpenDBConnection() *sql.DB {
	psqlInfo := fmt.Sprintf("host=%s port=%d user=%s password=%s dbname=%s sslmode=disable",
		pgHost, pgPort, pgUser, pgPwd, pgDatabase)

	db, err := sql.Open("postgres", psqlInfo)
	if err != nil {
		panic(err)
	}

	err = db.Ping()
	if err != nil {
		panic(err)
	}

	return db
}

func GETHandler(c *gin.Context) {
	db := OpenDBConnection()
	rows, err := db.Query("SELECT id, name, description, price FROM product")
	if err != nil {
		log.Fatal(err)
	}

	var products []Product
	for rows.Next() {
		var p Product
		rows.Scan(&p.ID, &p.Name, &p.Description, &p.Price)
		products = append(products, p)
	}
	c.IndentedJSON(http.StatusOK, products)

	defer rows.Close()
	defer db.Close()
}

func POSTHandler(c *gin.Context) {
	var p Product
	if err := c.BindJSON(&p); err != nil {
    	c.IndentedJSON(http.StatusBadRequest, "invalid product")
        return
    }

	db := OpenDBConnection()
	sqlStatement := `INSERT INTO product (name, description, price) VALUES ($1, $2, $3)`
	_, errDB := db.Exec(sqlStatement, p.Name, p.Description, p.Price)
	if errDB != nil {
    	c.IndentedJSON(http.StatusBadRequest, "error saving product")
		panic(errDB)
	}
	c.IndentedJSON(http.StatusOK, p)

	defer db.Close()
}

func main() {
	router := gin.Default()
    router.GET("/products", GETHandler)
    router.POST("/products", POSTHandler)

	routerHost := fmt.Sprintf("%s:%d", appHost, appPort)
    router.Run(routerHost)
}
