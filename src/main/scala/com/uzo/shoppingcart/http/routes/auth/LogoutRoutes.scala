package com.uzo.shoppingcart.http.routes.auth

import cats.effect.Sync
import cats.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import com.uzo.shoppingcart.algebras.Auth
import com.uzo.shoppingcart.domain.auth._
import com.uzo.shoppingcart.http.auth.users.CommonUser
import com.uzo.shoppingcart.http.decoder._
import com.uzo.shoppingcart.http.json._
import dev.profunktor.auth.AuthHeaders

final class LogoutRoutes[F[_]: Sync](
                                    auth: Auth[F]
                                    ) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/auth"

  private val httpRoutes : AuthedRoutes[CommonUser, F] =
    AuthedRoutes.of{
      case ar @ POST -> Root / "logout" as user =>
        AuthHeaders
        .getBearerToken(ar.req)
        .traverse_(t =>
          auth.logout(t, user.value.name)
        ) *> NoContent()
    }

  def routes(
              authMiddleware: AuthMiddleware[F, CommonUser]
            ): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}

/**
  * We are accessing the headers of the request fo find the current access token and invalidate it,
  * which means removing it form our cache, as we will see in the Auth Interpreter
  */
