package simplerest

object Models {

  case class Product(id: Int, name: String, description: String, price: Double)

  case class ProductInformation(id: Int, name: String)

  case class ShoppingCart(id: Int, total: Double, entries: List[ShoppingCartEntry])

  case class ShoppingCartEntry(id: Int, cart: ShoppingCart, product: Product, amount: Int)

}
