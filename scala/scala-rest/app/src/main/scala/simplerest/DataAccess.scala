package simplerest

import cats.effect.IO

trait DataAccess {
  def listProducts(): IO[List[Models.ProductInformation]]
  def productWithId(id: Int): IO[Models.Product]
  def newOrder(items: List[Models.ShoppingCartEntry]): IO[Models.ShoppingCart]
  def orderWithId(id: Int): IO[Models.ShoppingCart]
}
