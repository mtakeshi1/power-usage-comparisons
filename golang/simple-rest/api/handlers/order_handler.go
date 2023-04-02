package handlers

import (
	"net/http"
	"simple-rest/api/services"
	"strconv"

	"github.com/gin-gonic/gin"
)

func CreateOrder(c *gin.Context) {
	var entries []services.OrderEntry
	if err := c.BindJSON(&entries); err != nil {
		c.IndentedJSON(http.StatusBadRequest, "invalid order entries")
		return
	}

	orderId := services.CreateOrder(entries)
	c.IndentedJSON(http.StatusOK, orderId)
}

func GetOrderById(c *gin.Context) {
	orderId, _ := strconv.Atoi(c.Param("orderId"))

	order := services.GetOrderById(orderId)
	c.IndentedJSON(http.StatusOK, order)
}
