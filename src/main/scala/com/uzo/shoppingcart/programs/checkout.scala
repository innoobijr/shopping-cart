package com.uzo.shoppingcart.programs

import cats.Monad
import cats.effect.Timer
import cats.implicits._
import retry._
import retry.RetryPolicies._
import retry.RetryDetails._
import io.chrisdavenport.log4cats.Logger
import com.uzo.shoppingcart.algebras.{Orders, ShoppingCart}
import com.uzo.shoppingcart.domain.auth.UserId
import com.uzo.shoppingcart.domain.cart.CartItem
import com.uzo.shoppingcart.domain.checkout._
import com.uzo.shoppingcart.domain.order._
import com.uzo.shoppingcart.effects.Background
import com.uzo.shoppingcart.effects.effects.MonadThrow
import com.uzo.shoppingcart.http.clients.PaymentClient
import squants.market.Money

import scala.concurrent.duration._

final class CheckoutProgram[F[_]: Monad : Background: Logger: MonadThrow: Timer](
                                        paymentClient: PaymentClient[F],
                                        shoppingCart: ShoppingCart[F],
                                        orders: Orders[F]
                                        ) {

  //Retry policy
  val retryPolicy = limitRetries[F](3) |+| exponentialBackoff[F](10.milliseconds)
  def logError(action: String)(
              e: Throwable,
              details: RetryDetails
  ): F[Unit] = details match {
    case r: WillDelayAndRetry =>
      Logger[F].error(
        s"Failed on $action. We retried ${r.retriesSoFar} times."
      )
    case g: GivingUp =>
      Logger[F].error(
        s"Giving up on $action after ${g.totalRetries} retries."
      )
  }

  def processPayment(userId: UserId, total: Money, card: Card) : F[PaymentId] =
  {
    val action  = retryingOnAllErrors[PaymentId](
      policy = retryPolicy,
      onError = logError("Paymenrts")
    )(paymentClient.process(userId, total, card))

    action.adaptError{
      case e => PaymentError(e.getMessage)
    }
  }

  def createOrder(
                 userId: UserId,
                 paymentId: PaymentId,
                 items: List[CartItem],
                 total: Money
                 ) : F[OrderId] = {
    val action = retryingOnAllErrors[OrderId](
      policy =retryPolicy,
      onError = logError("Order")
    )(orders.create(userId, paymentId, items, total))

    action.adaptError {
      case e => OrderError(e.getMessage)
    }
      .onError{
        case _ =>
          Logger[F].error(
            s"Failed to create order for: ${paymentId}"
          ) *>
          Background[F].schedule(action, 1.hour)
      }
  }

  // Here we can see that the type system is trying to help us out.
  // flatmap over shoopingCart returns an Any
  /*
    There are many possible failures:
      1. shoppingCart.get(..)
      2. paymentClient.process(..)
      3. orders.create(...)
      4. soppingCart.delete(..)
   */
  def checkout(userId: UserId, card: Card): F[OrderId] =
    for {
      cart <- shoppingCart.get(userId)
      paymentId <- paymentClient.process(userId, cart.total, card)
      orderId <- orders.create(userId, paymentId, cart.total)
      _ <- shoppingCart.delete(userId)
    } yield orderId

}