package main

import (
	"fmt"
	"log"
	"net/http"
	"os"
	"simple-rest/api/handlers"

	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
	_ "github.com/lib/pq"
)

func main() {
	err := godotenv.Load()
	if err != nil {
		log.Fatal(".env file couldn't be loaded")
	}
	appHost := os.Getenv("APP_HOST")
	appPort := os.Getenv("APP_PORT")

	router := gin.Default()
	router.GET("/ping", PingHandler)

	router.GET("/products", handlers.GetAllProducts)
	router.POST("/products", handlers.CreateProduct)
	router.GET("/products/:productId", handlers.GetProductById)

	router.POST("/orders/new", handlers.CreateOrder)
	router.GET("/orders/:orderId", handlers.GetOrderById)

	routerHost := fmt.Sprintf("%s:%s", appHost, appPort)
	router.Run(routerHost)
}

func PingHandler(c *gin.Context) {
	c.IndentedJSON(http.StatusOK, handlers.Ping())
}
