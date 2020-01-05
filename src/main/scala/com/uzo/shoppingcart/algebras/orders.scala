package com.uzo.shoppingcart.algebras

import cats.effect._
import cats.implicits._

import com.uzo.shoppingcart.domain.auth._
import com.uzo.shoppingcart.domain.cart._
import com.uzo.shoppingcart.domain.item._
import com.uzo.shoppingcart.domain.order._
import com.uzo.shoppingcart.http.json._
import com.uzo.shoppingcart.ext.skunkx._

import squants.market._
import skunk._
import skunk.circe.codec.all._
import skunk.codec.all._
import skunk.implicits._
import squants.market._



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

object LiveOrders {
  def make[F[_]: Sync](
      sessionPool: Resource[F, Session[F]]
  ): F[Orders[F]] =
    Sync[F].delay(
      new LiveOrders[F](sessionPool)
    )
}

private class LiveOrders[F[_]: Sync](
    sessionPool: Resource[F, Session[F]]
) extends Orders[F] {
  import OrderQueries._

  def get(userId: UserId, orderId: OrderId): F[Option[Order]] =
    sessionPool.use { session =>
      session.prepare(selectByUserIdAndOrderId).use { q =>
        q.option(userId ~ orderId)
      }
    }

  def findBy(userId: UserId): F[List[Order]] =
    sessionPool.use { session =>
      session.prepare(selectByUserId).use { q =>
        q.stream(userId, 1024).compile.toList
      }
    }

  def create(
      userId: UserId,
      paymentId: PaymentId,
      items: List[CartItem],
      total: Money
  ): F[OrderId] =
    sessionPool.use { session =>
      session.prepare(insertOrder).use { cmd =>
        GenUUID[F].make[OrderId].flatMap { id =>
          val itMap = items.map(x => x.item.uuid -> x.quantity).toMap
          val order = Order(id, paymentId, itMap, total)
          cmd.execute(userId ~ order).as(id)
        }
      }
    }

}



private object OrderQueries {
  /**
   * We are ysing a new codec `jsonb`, which is backed by the Circe library. It takes a type
   * paramters A and requires instances of both io.cirece.Encoder and io.circe.Decode, to be inscopre of A.
   * To use this code, you need to add the extra dependecny skunk-circe and import
   * `import skunk.circe.codec.all._`.
   */
  val decoder : Decoder[Order] = (
    uuid.cimap[OrderId] ~ varchar ~ uuid.cimap[PaymentId] ~
    jsonb[Map[ItemId, Quantity]] ~ numeric.map(USD.apply)
  ).map{
    case o ~ _ ~ p ~ i ~ t => Order(o, p, i, t)
  }

  val selectByUserId: Query[UserId, Order] = 
    sql"""
      SELECT * FROM orders
      WHERE user_id = ${uuid.cimap[UserId]}
    """.query(decoder)

  val selectByUserIdAndOrderId: Query[UserId ~ OrderId, Order] = 
    sql"""
      SELECT * FROM orders
      WHERE user_id = ${uuid.cimap[UserId]}
      AND uuid = ${uuid.cimap[OrderId]}
    """.query(decoder)

    val encoder: Encoder[UserId ~ Order] =
    (
      uuid.cimap[OrderId] ~ uuid.cimap[UserId] ~ uuid.cimap[PaymentId] ~
      jsonb[Map[ItemId, Quantity]] ~ numeric.contramap[Money](_.amount)
    ).contramap {
      case id ~ o =>
      o.id ~ id ~ o.paymentId ~ o.items ~ o.total
    }

    val insertOrder: Command[UserId ~ Order] = 
    sql"""
      INSERT INTO orders
      VALUES ($encoder)
    """.command
}