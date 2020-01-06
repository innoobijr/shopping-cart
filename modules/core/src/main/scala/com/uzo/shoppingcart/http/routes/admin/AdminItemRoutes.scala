package com.uzo.shoppingcart.http.routes.admin

import cats.effect.Sync
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import com.uzo.shoppingcart.algebras.Items
import com.uzo.shoppingcart.domain.item.{CreateItemParam, UpdateItemParam}
import com.uzo.shoppingcart.http.auth.users.AdminUser
import com.uzo.shoppingcart.http.decoder._
import com.uzo.shoppingcart.http.json._


final class AdminItemRoutes[F[_]: Sync](
                                       items: Items[F]
                                       ) extends Http4sDsl[F] {
  private[admin] val prefixPath = "/items"

  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of {
      // Created a new item
      case ar @ POST -> Root as _ =>
        ar.req.decodeR[CreateItemParam]{ item =>
          Created(items.create(item.toDomain))
        }

      // Update price of item
      case ar @ PUT -> Root as _ =>
        ar.req.decodeR[UpdateItemParam]{ item =>
          Ok(items.update(item.toDomain))
        }
    }

  def routes(
            authMiddleware: AuthMiddleware[F, AdminUser]
            ): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
