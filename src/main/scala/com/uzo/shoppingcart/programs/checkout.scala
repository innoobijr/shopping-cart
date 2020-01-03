package com.uzo.shoppingcart.programs

import cats.Monad
import com.uzo.shoppingcart.algebras.{Orders, ShoppingCart}
import com.uzo.shoppingcart.domain.auth.UserId
import com.uzo.shoppingcart.domain.checkout.Card
import com.uzo.shoppingcart.domain.order.OrderId
import com.uzo.shoppingcart.http.clients.PaymentClient


final class CheckoutProgram[F[_]: Monad](
                                        paymentClient: PaymentClient[F],
                                        shoppingCart: ShoppingCart[F],
                                        orders: Orders[F]
                                        ) {
  def checkout(userId: UserId, card: Card): F[OrderId] =
    for {
      cart <- shoppingCart.get(userId)
      paymentId <- paymentClient.process(userId, cart.total, card)
      orderId <- orders.create(userId, paymentId, cart.total)
      _ <- shoppingCart.delete(userId)
    } yield orderId

}