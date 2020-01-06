package com.uzo.shoppingcart.http.routes.admin

import cats.effect.Sync
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import com.uzo.shoppingcart.algebras.Brands
import com.uzo.shoppingcart.domain.brand._
import com.uzo.shoppingcart.http.auth.users.AdminUser
import com.uzo.shoppingcart.http.decoder._
import com.uzo.shoppingcart.http.json._

final class AdminBrandRoutes[F[_]: Sync](
                                        brands: Brands[F]
                                        ) extends Http4sDsl[F] {
  private[admin] val prefixPath = "/brands"

  private val httpRoutes : AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of{
      //Using decodeR, we validate the brand received in our request body is not empty.
      case ar @ POST -> Root as _ =>
        ar.req.decodeR[BrandParam]{ bp =>
          Created(brands.create(bp.toDomain))
        }

    }

  def routes(
            authMiddleware: AuthMiddleware[F, AdminUser]
            ): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
