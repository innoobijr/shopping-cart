package com.uzo.shoppingcart.algebras

import com.uzo.shoppingcart.domain.auth.UserId
import com.uzo.shoppingcart.domain.cart.CartItem
import com.uzo.shoppingcart.domain.order.{Order, OrderId, PaymentId}
import squants.market.Money

/**
  * Once we process a payment, we need ot persist the order, we also
  * wnat to be able ot query pass orders. Here is our algebra:
  *
  * @tparam F
  */
trait Orders[F[_]]{
  def get(
         userId: UserId,
         orderId: OrderId
         ): F[Option[Order]]

  def findBy(userId: UserId): F[List[Order]]

  def create(
            userId: UserId,
            paymentId: PaymentId,
            items: List[CartItem],
            total: Money
            ): F[OrderId]
}