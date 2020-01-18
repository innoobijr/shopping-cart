package com.uzo.shoppingcart.modules

import cats.Parallel
import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats.algebra.RedisCommands
import skunk._
import com.uzo.shoppingcart.algebras._
import com.uzo.shoppingcart.config.data._

object Algebras{
  def make[F[_] : Concurrent: Parallel: Timer](
    redis: RedisCommands[F, String, String],
    sessionPool: Resource[F, Session[F]],
    cartExpiration: ShoppingCartExpiration
  ) : F[Algebras[F]] = 
    for {
      brands <- LiveBrands.make[F](sessionPool)
      categories <- LiveCategories.make[F](sessionPool)
      items <- LiveItems.make[F](sessionPool)
      cart <- LiveShoppingCart.make[F](items, redis, cartExpiration)
      orders <- LiveOrders.make[F](sessionPool)
      health <- LiveHealthCheck.make[F](sessionPool, redis)
    } yield new Algebras[F](cart, brands, categories, items, orders, health)
}

/**
 * Our `Algebras` class is the interface we will be using in `Main` as well as in other modules.
 * We make it final becuase no other component should extends it, and its smart constructor just initializes all the `Live` interpreters of the algebras we need
 */

final class Algebras[F[_]] private(
  val cart: ShoppingCart[F],
  val brands: Brands[F],
  val categories: Categories[F],
  val items: Items[F],
  val orders: Orders[F],
  val healthCheck: HealthCheck[F]
  ){}
