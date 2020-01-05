package com.uzo.shoppingcart.http.routes.admin

import cats.effect.Sync
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import com.uzo.shoppingcart.algebras.Categories
import com.uzo.shoppingcart.domain.category.CategoryParam
import com.uzo.shoppingcart.http.auth.users.AdminUser
import com.uzo.shoppingcart.http.decoder._
import com.uzo.shoppingcart.http.json._

final class AdminCategoryRoutes[F[_]: Sync](categories: Categories[F]) extends Http4sDsl[F] {

  private [admin] val prefixPath = "/categories"

  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of{

      case ar @ POST -> Root as _ =>
        ar.req.decodeR[CategoryParam]{ c =>
          Created(categories.create(c.toDomain))
        }
    }

  def routes(
              authMiddleware: AuthMiddleware[F, AdminUser]
            ): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
