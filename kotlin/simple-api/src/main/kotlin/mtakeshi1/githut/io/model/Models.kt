package mtakeshi1.githut.io.model

import kotlinx.serialization.Serializable

object Models {

    @Serializable
    data class Product(val id: Int, val name: String, val description: String, val price: Double)

    @Serializable
    data class SmallProduct(val id: Int, val name: String)

    @Serializable
    data class ShoppingCart(val id: Int, val total: Double, val entries: List<ShoppingCartEntry>)

    @Serializable
    data class ShoppingCartEntry(val productId: Int, val amount: Int)

}