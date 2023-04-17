package simplerest

import skunk.{Session, ~, *}
import cats.effect.*
import cats.implicits._
import skunk.implicits.*
import skunk.codec.all.*
import natchez.Trace.Implicits.noop
import simplerest.Models.ProductInformation

class SkunkDataAccess(val host: String, val user: String, val pwd: String, val database: String) extends DataAccess {

  private val pool = Session.pooled[IO](host = host, user = user, password = Some(pwd), database = database, max = 20)

  val listProductsQuery: Query[Void, ProductInformation] = sql"select id,name from product".query(int4 ~ varchar(255)).gmap[ProductInformation]

  val productById: Query[Int, Models.Product] = sql"select id, name, description, price from product where id = $int4".query(int4 ~ varchar(255) ~ varchar(255) ~ float8).gmap[Models.Product]

  case class ShoppingCartWithItems(id: Int, total: Double, productId: Int, amount: Int)

  val orderById: Query[Int, ShoppingCartWithItems] = sql"select c.id,c.total,i.product_id, i.amount from shoppingcart c join productorder i on c.id = i.shoppingcart_id where c.id = $int4 ".query(int4 ~ float8 ~ int4 ~ int4).gmap[ShoppingCartWithItems]

  override def listProducts(): IO[List[Models.ProductInformation]] = {
    pool.use {_.use { session =>
        for {
          list <- session.execute(listProductsQuery)
        } yield list
      }}
  }

  override def productWithId(id: Int): IO[Models.Product] = {
    pool.use {_.use { session => {
        session.unique(productById, id)
      }}}
  }

  val insertMany: Command[List[Int ~ Int ~ Int]] = {
    val pars = (int4 ~ int4 ~ int4).values.list(3)
    sql"insert into productorder(amount, product_id, shoppingcart_id) values $pars".command
  }

  val insertOrderCmd: Query[Double, Int] = sql"insert into shoppingcart (id, total) values (DEFAULT, $float8) returning id".query(int4)

  override def newOrder(items: List[Models.ShoppingCartEntry]): IO[Models.ShoppingCart] = {
    pool.use {
      _.use { session =>
        session.transaction.use { tx =>
          for {
            prods <- items.map(i => productWithId(i.productId).map(prod => (prod, i.amount))).sequence
            total = prods.map(t => t._2 * t._1.price).sum
            orderId <- session.unique(insertOrderCmd, total)
            _ <- session.execute(insertMany, items.map(spe => spe.amount ~ spe.productId ~ orderId))
          } yield Models.ShoppingCart(orderId, total, items)
        }
      }
    }
  }

  override def orderWithId(id: Int): IO[Models.ShoppingCart] = {
    pool.use {
      _.use { session =>
        for {
          list <- session.execute(orderById, id)
        } yield {
          Models.ShoppingCart(list.head.id, list.head.total, list.map(c => Models.ShoppingCartEntry(c.productId, c.amount)))
        }
      }
    }
  }
}
