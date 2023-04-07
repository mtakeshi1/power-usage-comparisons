package simplerest

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import cats.syntax.all.*
import com.comcast.ip4s.*
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.circe.jsonOf
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.{Router, Server}
import simplerest.Models.{Product, ShoppingCart}

import scala.concurrent.duration.*

object Main {

  case class OrderEntry(productId: Int, amount: Int)

  implicit val circe: Decoder[OrderEntry] = deriveDecoder[OrderEntry]
  implicit val decoder: EntityDecoder[IO, OrderEntry] = jsonOf[IO, OrderEntry]
  implicit val listDecoder: EntityDecoder[IO, List[OrderEntry]] = jsonOf[IO, List[OrderEntry]]

  val helloServices: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name => Ok(s"Hello, $name.")
  }

  val prodductsService: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => Ok(s"Hello products /")
    case GET -> Root / id => Ok(s"Hello, product with id $id.")
  }

  val ordersServices: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / id => Ok(s"Hello, order - $id.")
    case req@POST -> Root / "new" =>
      for {
        entries <- req.as[List[OrderEntry]]
        resp <- Ok(s"Hello, post new order $entries")
      } yield resp
  }


  def main(args: Array[String]): Unit = {
    import cats.effect.unsafe.implicits.global
    val httpApp = Router(
                          "/" -> helloServices,
                          "/products" -> prodductsService,
                          "/orders" -> ordersServices).orNotFound
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
