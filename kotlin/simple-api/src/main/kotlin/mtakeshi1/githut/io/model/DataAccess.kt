package mtakeshi1.githut.io.model

interface DataAccess {

    fun listProducts(): List<Models.SmallProduct>
    fun productWithId(id: Int): Models.Product

    fun newOrder(items: List<Models.ShoppingCartEntry>): Models.ShoppingCart
    fun orderWithId(id: Int): Models.ShoppingCart

}