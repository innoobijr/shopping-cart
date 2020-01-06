package com.uzo.shoppingcart.http.routes.auth

import cats.effect.Sync
import cats.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import com.uzo.shoppingcart.algebras.Auth
import com.uzo.shoppingcart.domain.auth._
import com.uzo.shoppingcart.http.decoder._
import com.uzo.shoppingcart.http.json._


final class LoginRoutes[F[_]: Sync](
                                   auth: Auth[F]
                                   ) extends Http4sDsl[F]{

  private[routes] val prefixPath = "/auth"

  private val httpsRoutes: HttpRoutes[F] = HttpRoutes.of[F]{
    case req @ POST -> Root / "login" =>
      req.decodeR[LoginUser]{ user =>
        auth
          .login(user.username.toDomain, user.password.toDomain)
          .flatMap(Ok(_))
          .handleErrorWith{
            case InvalidUserOrPassword(_) => Forbidden()
          }
      }
  }

  val routes : HttpRoutes[F] = Router(
    prefixPath -> httpsRoutes
  )
}
