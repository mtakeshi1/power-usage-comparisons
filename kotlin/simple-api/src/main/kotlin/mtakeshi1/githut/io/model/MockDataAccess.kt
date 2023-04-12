package mtakeshi1.githut.io.model

object MockDataAccess : DataAccess {

    private val products = listOf(Models.Product(1, "a", "aa", 10.0), Models.Product(2, "b", "bb", 20.0))

    private val orders = mutableListOf<Models.ShoppingCart>()

    override fun listProducts(): List<Models.Product> = products

    override fun productWithId(id: Int): Models.Product = products.filter { it.id == id }[0]

    override fun newOrder(items: List<Models.ShoppingCartEntry>): Models.ShoppingCart {
        val order =
            Models.ShoppingCart(orders.size, items.map { it.amount * productWithId(it.productId).price }.sum(), items)
        orders.add(order)
        return order
    }

    override fun orderWithId(id: Int): Models.ShoppingCart = orders.filter { it.id == id }[0]

}