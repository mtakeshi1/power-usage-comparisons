package handlers

import (
	"fmt"
	"net/http"
	"simple-rest/api/services"
	"strconv"

	"github.com/gin-gonic/gin"
)

func Ping() string {
	bla := fmt.Sprintf("ping = %s ", "pong")
	return bla
}

func GetAllProducts(c *gin.Context) {
	products := services.GetAllProducts()
	c.IndentedJSON(http.StatusOK, products)
}

func CreateProduct(c *gin.Context) {
	var p services.Product
	if err := c.BindJSON(&p); err != nil {
		c.IndentedJSON(http.StatusBadRequest, "invalid product")
		return
	}

	product, err := services.CreateProduct(&p)
	if err != nil {
		c.IndentedJSON(http.StatusInternalServerError, "error saving product")
		panic(err)
	}
	c.IndentedJSON(http.StatusOK, product)
}

func GetProductById(c *gin.Context) {
	productId, _ := strconv.Atoi(c.Param("productId"))

	product := services.GetProductById(productId)
	c.IndentedJSON(http.StatusOK, product)
}
