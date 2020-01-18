package com.uzo.shoppingcart.modules

import cats.effect._
import org.http4s.client.Client
import com.uzo.shoppingcart.config.data.PaymentConfig
import com.uzo.shoppingcart.http.clients._

object HttpClients{
  def make[F[_]: Sync](
    cfg: PaymentConfig,
    client: Client[F]
  ): F[HttpClients[F]] = 
    Sync[F].delay(
      new HttpClients[F]{
        def payment: PaymentClient[F] = new LivePaymentClient[F](cfg, client)
      }
    )
}

// Could you use a SAM here?
// Can this be a sealed abstract class instead
// of a Trait?
trait HttpClients[F[_]]{
  def payment: PaymentClient[F]
}
