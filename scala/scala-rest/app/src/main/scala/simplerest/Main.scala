package simplerest

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import cats.syntax.all.*
import com.comcast.ip4s.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.circe.jsonOf
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.{Router, Server}
import simplerest.Models.{Product, ShoppingCart}

import scala.concurrent.duration.*

object Main {

  import io.circe.generic.auto.*
  import io.circe.syntax.*
  import org.http4s.circe._

//  implicit val circe: Decoder[Models.ShoppingCartEntry] = io.circe.generic.semiauto.deriveDecoder[Models.ShoppingCartEntry]
  //  implicit val decoder: EntityDecoder[IO, OrderEntry] = jsonOf[IO, OrderEntry]
  implicit val ordersDecoder: EntityDecoder[IO, List[Models.ShoppingCartEntry]] = jsonOf[IO, List[Models.ShoppingCartEntry]]
  //  implicit val productsEncoder: EntityEncoder[IO, List[Models.ProductInformation]] =
  //  implicit val ordersEncoder: EntityEncoder[IO, List[OrderEntry]] = jsonOf[IO, List[OrderEntry]]
  //
  //  implicit val productInfoEncoder: Encoder[Models.ProductInformation] = deriveEncoder[Models.ProductInformation]
  //  implicit val productEncoder: Encoder[Models.Product] = deriveEncoder[Models.Product]
  //
  //  implicit val orderEncoder: Encoder[Models.ShoppingCart] = deriveEncoder[Models.ShoppingCart]

  val helloServices: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name => Ok(s"Hello, $name.")
  }

  def productsService(access: DataAccess): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => {
      for {
        products <- access.listProducts()
        r <- Ok(products.asJson)
      } yield r
    }
    case GET -> Root / id => for {
      product <- access.productWithId(id.toInt)
      r <- Ok(product.asJson)
    } yield r
  }

  def ordersServices(access: DataAccess): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / id => {
      for {
        order <- access.orderWithId(id.toInt)
        resp <- Ok(order.asJson)
      } yield resp
    }
    case req@POST -> Root / "new" =>
      for {
        entries <- req.as[List[Models.ShoppingCartEntry]]
        order <- access.newOrder(entries)
        resp <- Ok(s"$order.id")
      } yield resp
  }


  def main(args: Array[String]): Unit = {
    import cats.effect.unsafe.implicits.global
    val data: DataAccess = ???
    val httpApp = Router("/" -> helloServices,
      "/products" -> productsService(data),
      "/orders" -> ordersServices(data)).orNotFound
    val server: Resource[IO, Server] = EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
    val shutdown = server.allocated.unsafeRunSync()._2
    Thread.sleep(100000)
  }


}
