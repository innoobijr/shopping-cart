package com.uzo.shoppingcart.http.clients

import com.uzo.shoppingcart.domain.auth.UserId
import com.uzo.shoppingcart.domain.order.PaymentId
import javax.smartcardio.Card
import squants.market.Money

trait PaymentClient[F[_]] {
  def process(userId: UserId, total: Money, card: Card): F[PaymentId]
}