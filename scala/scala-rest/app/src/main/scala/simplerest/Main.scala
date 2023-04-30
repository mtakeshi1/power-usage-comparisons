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

object Main extends IOApp.Simple {

  import io.circe.generic.auto.*
  import io.circe.syntax.*
  import org.http4s.circe.*

  implicit val ordersDecoder: EntityDecoder[IO, List[Models.ShoppingCartEntry]] = jsonOf[IO, List[Models.ShoppingCartEntry]]

  val helloServices: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name => Ok(s"Hello, $name.")
  }

  def productsService(access: IO[DataAccess]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => {
      for {
        acc <- access
        products <- acc.listProducts()
        r <- Ok(products.asJson)
      } yield r
    }
    case GET -> Root / id => for {
      acc <- access
      product <- acc.productWithId(id.toInt)
      r <- Ok(product.asJson)
    } yield r
  }

  def ordersServices(access: IO[DataAccess]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / id => {
      for {
        acc <- access
        order <- acc.orderWithId(id.toInt)
        resp <- Ok(order.asJson)
      } yield resp
    }
    case req@POST -> Root / "new" =>
      for {
        acc <- access
        entries <- req.as[List[Models.ShoppingCartEntry]]
        order <- acc.newOrder(entries)
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

  def main_(args: Array[String]): Unit = {
    import cats.effect.unsafe.implicits.global
    import org.http4s.server.middleware.{ErrorAction, ErrorHandling}

    val data: IO[DataAccess] = buildDataAccess()
    val httpApp = Router("/" -> helloServices,
      "/products" -> productsService(data),
      "/orders" -> ordersServices(data)).orNotFound
    val withErrorLogging = ErrorHandling.Recover.total(
      ErrorAction.log(
        httpApp,
        messageFailureLogAction = (t, msg) =>
          IO.println(msg) >>
            IO.println(t),
        serviceErrorLogAction = (t, msg) =>
          IO.println(msg) >>
            IO.println(t)
      )
    )


    val server: Resource[IO, Server] = EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(withErrorLogging)
      .build
    val shutdown = server.allocated.unsafeRunSync()._2
    while (System.in.available() == 0) Thread.sleep(100)
  }



  override def run: IO[Unit] = {
    import cats.effect.unsafe.implicits.global
    import org.http4s.server.middleware.{ErrorAction, ErrorHandling}

    val data: IO[DataAccess] = buildDataAccess()
    val httpApp = Router("/" -> helloServices,
      "/products" -> productsService(data),
      "/orders" -> ordersServices(data)).orNotFound
    val withErrorLogging = ErrorHandling.Recover.total(
      ErrorAction.log(
        httpApp,
        messageFailureLogAction = (t, msg) =>
          IO.println(msg) >>
            IO.println(t),
        serviceErrorLogAction = (t, msg) =>
          IO.println(msg) >>
            IO.println(t)
      )
    )


    val server: Resource[IO, Server] = EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(withErrorLogging)
      .build
    server.allocated.unsafeRunSync()._2
  }
}
