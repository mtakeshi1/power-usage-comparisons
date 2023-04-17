package simplerest

import cats.effect.IO
import cats.implicits._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object MockDataAccess extends DataAccess {

  private val products = List(Models.Product(0, "name", "desc", 1), Models.Product(1, "name1", "desc1", 2))
  private val orders = ListBuffer[Models.ShoppingCart]()

  override def listProducts(): IO[List[Models.ProductInformation]] = IO.pure(products.map(p => Models.ProductInformation(p.id, p.name)))

  override def productWithId(id: Int): IO[Models.Product] = IO.pure(products.find(_.id == id).get)

  override def newOrder(items: List[Models.ShoppingCartEntry]): IO[Models.ShoppingCart] = {
    for {
      prods <- items.map(i => productWithId(i.productId).map(prod => (prod, i.amount))).sequence
      total = prods.map(t => t._2 * t._1.price).sum
      order = Models.ShoppingCart(orders.size, total, items)
      _ = orders.addOne(order)
    } yield order
  }

  override def orderWithId(id: Int): IO[Models.ShoppingCart] = IO.pure(orders.find(_.id == id).get)
}
