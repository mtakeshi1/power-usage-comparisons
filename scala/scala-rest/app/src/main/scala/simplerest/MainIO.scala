package simplerest

import cats.effect.*
import cats.syntax.all.*
import com.comcast.ip4s.*
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.*
import org.http4s.circe.jsonOf
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.{Router, Server}
import simplerest.Models.{Product, ShoppingCart}

import java.io.{File, FileInputStream}
import java.util.Properties
import scala.concurrent.duration.*

object MainIO extends IOApp.Simple {

  import io.circe.generic.auto.*
  import io.circe.syntax.*
  import org.http4s.circe.*

  implicit val ordersDecoder: EntityDecoder[IO, List[Models.ShoppingCartEntry]] = jsonOf[IO, List[Models.ShoppingCartEntry]]

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
        resp <- Ok(s"${order.id}")
      } yield resp
  }

  def loadEnv(): Properties = {
    val props = new Properties()
    val f = new File(".env")
    if (f.exists()) {
      val fin = new FileInputStream(f)
      try {
        props.load(fin)
      } finally {
        fin.close()
      }
    }
    System.getenv().forEach((key, value) => props.put(key, value))
    props
  }

  def buildDataAccess(): IO[DataAccess] = {
    val env = loadEnv()
    if (env.containsKey("PG_HOST")) {
      SkunkDataAccess(env.getProperty("PG_HOST"), env.getProperty("PG_USER"), env.getProperty("PG_PWD"), env.getProperty("PG_DATABASE")).use(da => IO.delay(da))
    } else IO.pure(MockDataAccess)
  }


  override def run: IO[Unit] = {
    import cats.effect.Async
    import cats.effect.unsafe.implicits.global
    import cats.syntax.all.*
    import org.http4s.implicits.*
    import org.http4s.server.middleware.{ErrorAction, ErrorHandling, Logger}
    for {
      data <- buildDataAccess()
      httpApp = Router("/" -> helloServices,
        "/products" -> productsService(data),
        "/orders" -> ordersServices(data)).orNotFound
      finalHttpApp = Logger.httpApp(true, true)(httpApp)
      _ <-
        EmberServerBuilder.default[IO]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
          .useForever
    } yield ()
  }
}
