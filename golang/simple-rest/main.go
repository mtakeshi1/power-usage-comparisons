package main

import (
	"fmt"
	"net/http"
	"simple-rest/api/db"
	"simple-rest/api/handlers"
	"simple-rest/api/utils"

	"github.com/gin-gonic/gin"
	_ "github.com/lib/pq"
)

func main() {
	utils.LoadEnvParams()
	db.PingDB()

	appHost := utils.GetEnvString("APP_HOST")
	appPort := utils.GetEnvString("APP_PORT")
	routerHost := fmt.Sprintf("%s:%s", appHost, appPort)
	router := createRouter()
	router.Run(routerHost)
}

func createRouter() *gin.Engine {
	router := gin.Default()
	router.GET("/ping", PingHandler)

	router.GET("/products", handlers.GetAllProducts)
	router.POST("/products", handlers.CreateProduct)
	router.GET("/products/:productId", handlers.GetProductById)

	router.POST("/orders/new", handlers.CreateOrder)
	router.GET("/orders/:orderId", handlers.GetOrderById)

	return router
}

func PingHandler(c *gin.Context) {
	c.IndentedJSON(http.StatusOK, handlers.Ping())
}
