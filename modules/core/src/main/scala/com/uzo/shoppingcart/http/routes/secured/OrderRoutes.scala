package com.uzo.shoppingcart.http.routes.secured

import cats.effect.Sync
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import com.uzo.shoppingcart.algebras.Orders
import com.uzo.shoppingcart.domain.order._
import com.uzo.shoppingcart.http.auth.users.CommonUser
import com.uzo.shoppingcart.http.json._


final class OrderRoutes[F[_]: Sync](
                                   orders: Orders[F]
                                   ) extends Http4sDsl[F]{

  private[routes] val prefixPath = "/orders"

  private val httpRoutes : AuthedRoutes[CommonUser, F] =
    AuthedRoutes.of {
      case GET -> Root as user => Ok(orders.findBy(user.value.id))
      case GET -> Root / UUIDVar(orderId) as user =>
        Ok(orders.get(user.value.id, OrderId(orderId)))
    }

  def routes(
            authMiddleware: AuthMiddleware[F, CommonUser],
            ): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
