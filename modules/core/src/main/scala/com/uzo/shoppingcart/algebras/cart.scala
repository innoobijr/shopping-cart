package com.uzo.shoppingcart.algebras

import cats.effect.Sync
import cats.implicits._
import com.uzo.shoppingcart.domain.auth._
import com.uzo.shoppingcart.domain.cart._
import com.uzo.shoppingcart.domain.item._
import com.uzo.shoppingcart.effects._
import squants.market._
import com.uzo.shoppingcart.config.data.ShoppingCartExpiration
import dev.profunktor.redis4cats.algebra.RedisCommands
import com.uzo.shoppingcart.domain.auth
import com.uzo.shoppingcart.domain.item



trait ShoppingCart[F[_]]{
  def add(userId: UserId, itemId: ItemId, quantity: Quantity): F[Unit]
  def get(userId: UserId): F[CartTotal]
  def delete(userId: UserId): F[Unit]
  def removeItem(userId: UserId, itemId: ItemId): F[Unit]
  def update(userId: UserId, cart: Cart): F[Unit]
}

// safe constrcutor 
object LiveShoppingCart {
  def make[F[_]: Sync](
      items: Items[F],
      redis: RedisCommands[F, String, String],
      exp: ShoppingCartExpiration
  ): F[ShoppingCart[F]] =
    Sync[F].delay(
      new LiveShoppingCart(items, redis, exp)
    )
}

//Shopping Cart interpreter
final class LiveShoppingCart[F[_]: GenUUID: MonadThrow] private (
  items: Items[F],
  redis: RedisCommands[F, String, String],
  exp: ShoppingCartExpiration
) extends ShoppingCart[F]{

  def add(
    userId: UserId, 
    itemId: ItemId,
    quantity: Quantity
  ): F[Unit] = 
    redis.hSet(
      userId.value.toString,
      itemId.value.toString,
      quantity.value.toString
    ) *> 
    redis.expire(
      userId.value.toString,
      exp.value
    )

  /**
   *  Tries to find the shopping cart of the user using the hGetAll function, which returns
   * a `Map[K, V]`. It is exists it parses both filed and vlaues into a List[CartItem] and finally.
   * It calculates the totla amount. GenUUID[F].read takes a String and returns an F[A] where A: 
   * Coercible[UUID, *]], in this case, ItemId.
   * 
   */
  def get(userId: UserId): F[CartTotal] = 
    redis.hGetAll(userId.value.toString).flatMap{ it =>
      it.toList
        .traverseFilter{
          case (k, v) => 
            for {
              id <- GenUUID[F].read[ItemId](k)
              qt <- ApThrow[F].catchNonFatal(Quantity(v.toInt))
              rs <- items.findById(id).map(_.map(i => CartItem(i, qt)))
            } yield rs
        }
        .map(items => CartTotal(items, calcTotal(items)))
    }

    private def calcTotal(items: List[CartItem]): Money = 
    USD(items
      .foldMap{ i =>
        i.item.price.value * i.quantity.value
      }
    )

    def delete(userId: auth.UserId): F[Unit] = redis.del(userId.valid.toString)

    def removeItem(userId: auth.UserId, itemId: item.ItemId): F[Unit] = 
      redis.hDel(userId.value.toString, itemId.value.toString)

    /**
     * Retrieves the shoppign cart for the user (if it exists) and it updates teh quantituy
     * of each matching item, following by updating the shopping cart expiration.
     */
    def update(userId: auth.UserId, cart: Cart): F[Unit] = 
      redis.hGetAll(userId.value.toString).flatMap{it =>
        it.toList.traverse_ {
          case (k, _) => 
            GenUUID[F].read[ItemId](k).flatMap{ id =>
              cart.items.get(id).traverse_ { q =>
                redis.hSet(userId.value.toString, k, q.value.toString)
              }
            }
          } *>
            redis.expire(userId.value.toString, exp.value)
          }

    
}