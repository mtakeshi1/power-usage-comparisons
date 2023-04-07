package simplerest


import cats.data.{Kleisli, OptionT}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import org.http4s.{Request, Response}
import simplerest.Models.*

object API {

  implicit val productEncoder: Encoder[Product] = deriveEncoder[Product]
  implicit val productInformationEncoder: Encoder[ProductInformation] = deriveEncoder[ProductInformation]
  implicit val cartEntryEncoder: Encoder[ShoppingCartEntry] = deriveEncoder[ShoppingCartEntry]
  implicit val cartEncoder: Encoder[ShoppingCart] = deriveEncoder[ShoppingCart]
  implicit val cartEntryDecoder: Decoder[ShoppingCartEntry] = deriveDecoder[ShoppingCartEntry]

  type Http[F[_], G[_]] = Kleisli[F, Request[G], Response[G]]
  type HttpApp[F[_]] = Http[F, F]
//  type HttpRoutes[F[_]] = Http[OptionT[F, _], F]

}
