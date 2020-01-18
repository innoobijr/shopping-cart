package com.uzo.shoppingcart.http.clients

import cats.effect.Sync
import cats.implicits._
import com.uzo.shoppingcart.domain.auth.UserId
import com.uzo.shoppingcart.domain.checkout.Card
import com.uzo.shoppingcart.domain.order.{PaymentError, PaymentId}
import org.http4s.{Status, Uri}
import org.http4s.client.Client
import squants.market.Money
import com.uzo.shoppingcart.http.json._
import com.uzo.shoppingcart.config.data.PaymentConfig

trait PaymentClient[F[_]] {
  def process(userId: UserId, total: Money, card: Card): F[PaymentId]
}

class LivePaymentClient[F[_]: Sync](
  cfg: PaymentConfig,
  client: Client[F] ) extends PaymentClient[F]{

  def process(
             userId: UserId,
             total: Money,
             card: Card
             ): F[PaymentId] =
    Uri
      .fromString(cfg.uri.value.value + "/payments")
      .liftTo[F]
      .flatMap { uri =>
        client.get[PaymentId](uri){ r =>
          if(r.status == Status.Ok || r.status == Status.Conflict)
            r.as[PaymentId]
          else
            PaymentError(r.status.reason).raiseError[F, PaymentId]
        }
      }

}
