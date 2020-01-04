package com.uzo.shoppingcart.http

import cats.effect.Sync
import dev.profunktor.auth.jwt.JwtToken
import io.circe._
import io.circe.generic.semiauto._
import io.circe.refined._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import org.http4s.{ EntityDecoder, EntityEncoder }
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import com.uzo.shoppingcart.domain.auth._
import com.uzo.shoppingcart.domain.brand._
import com.uzo.shoppingcart.domain.cart._
import com.uzo.shoppingcart.domain.category._
import com.uzo.shoppingcart.domain.checkout._
//import com.uzo.shoppingcart.domain.healthcheck._
import com.uzo.shoppingcart.domain.item._
import com.uzo.shoppingcart.domain.order._
import com.uzo.shoppingcart.ext.refined._
import com.uzo.shoppingcart.http.auth.users._
import squants.market._

object json {

  implicit def jsonDecoder[F[_]: Sync, A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
  implicit def jsonEncoder[F[_]: Sync, A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  // ----- Overriding some Coercible codecs ----
  implicit val brandParamDecoder: Decoder[BrandParam] =
    Decoder.forProduct1("name")(BrandParam.apply)

  implicit val itemIdDecoder: Decoder[ItemId] =
    Decoder.forProduct1("value")(ItemId.apply)

  implicit val itemIdEncoder: Encoder[ItemId] =
    Encoder.forProduct1("value")(_.value)

  implicit val paymentIdEncoder: Encoder[PaymentId] =
    Encoder.forProduct1("value")(_.value)

  implicit val paymentIdDecoder: Decoder[PaymentId] =
    Decoder.forProduct1("value")(PaymentId.apply)


  implicit val orderIdDecoder: Decoder[OrderId] =
    Decoder.forProduct1("value")(OrderId.apply)

  implicit val orderIdEncoder: Encoder[OrderId] =
    Encoder.forProduct1("value")(_.value)

  implicit val quantityDecoder: Decoder[Quantity] =
    Decoder.forProduct1("value")(Quantity.apply)

  implicit val quantityEncoder: Encoder[Quantity] =
    Encoder.forProduct1("value")(_.value)

  implicit val categoryParamEncoder: Encoder[CategoryParam] =
    Encoder.forProduct1("value")(_.value)

  implicit val categoryParamDecoder: Decoder[CategoryParam] =
    Decoder.forProduct1("value")(CategoryParam.apply)

  implicit val categoryNameEncoder: Encoder[CategoryName] =
    Encoder.forProduct1("value")(_.value)

  implicit val categoryNameDecoder: Decoder[CategoryName] =
    Decoder.forProduct1("value")(CategoryName.apply)


  // ----- Coercible codecs -----
  implicit def coercibleDecoder[A: Coercible[B, ?], B: Decoder]: Decoder[A] =
    Decoder[B].map(_.coerce[A])

  implicit def coercibleEncoder[A: Coercible[B, ?], B: Encoder]: Encoder[A] =
    Encoder[B].contramap(_.repr.asInstanceOf[B])

  implicit def coercibleKeyDecoder[A: Coercible[B, ?], B: KeyDecoder]: KeyDecoder[A] =
    KeyDecoder[B].map(_.coerce[A])

  implicit def coercibleKeyEncoder[A: Coercible[B, ?], B: KeyEncoder]: KeyEncoder[A] =
    KeyEncoder[B].contramap[A](_.repr.asInstanceOf[B])

  // ----- Domain codecs -----

  implicit val brandDecoder: Decoder[Brand] = deriveDecoder[Brand]
  implicit val brandEncoder: Encoder[Brand] = deriveEncoder[Brand]

  implicit val categoryDecoder: Decoder[Category] = deriveDecoder[Category]
  implicit val categoryEncoder: Encoder[Category] = deriveEncoder[Category]

  implicit val moneyDecoder: Decoder[Money] =
    Decoder[BigDecimal].map(USD.apply)

  implicit val moneyEncoder: Encoder[Money] =
    Encoder[BigDecimal].contramap(_.amount)

  implicit val itemDecoder: Decoder[Item] = deriveDecoder[Item]
  implicit val itemEncoder: Encoder[Item] = deriveEncoder[Item]

  implicit val createItemDecoder: Decoder[CreateItemParam] = deriveDecoder[CreateItemParam]
  implicit val updateItemDecoder: Decoder[UpdateItemParam] = deriveDecoder[UpdateItemParam]

  implicit val cartItemDecoder: Decoder[CartItem] = deriveDecoder[CartItem]
  implicit val cartItemEncoder: Encoder[CartItem] = deriveEncoder[CartItem]

  implicit val cartTotalEncoder: Encoder[CartTotal] = deriveEncoder[CartTotal]

  implicit val orderEncoder: Encoder[Order] = deriveEncoder[Order]

  implicit val cardDecoder: Decoder[Card] = deriveDecoder[Card]

  implicit val tokenEncoder: Encoder[JwtToken] =
    Encoder.forProduct1("access_token")(_.value)

  implicit val cartEncoder: Encoder[Cart] =
    Encoder.forProduct1("items")(_.items)

  implicit val cartDecoder: Decoder[Cart] =
    Decoder.forProduct1("items")(Cart.apply)

  implicit val userDecoder: Decoder[User] = deriveDecoder[User]
  implicit val userEncoder: Encoder[User] = deriveEncoder[User]

  //implicit val appStatusEncoder: Encoder[AppStatus] = deriveEncoder[AppStatus]

  implicit val createUserDecoder: Decoder[CreateUser] = deriveDecoder[CreateUser]

  implicit val loginUserDecoder: Decoder[LoginUser] = deriveDecoder[LoginUser]

}