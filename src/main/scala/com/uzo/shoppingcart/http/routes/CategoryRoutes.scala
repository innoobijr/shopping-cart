package com.uzo.shoppingcart.http.routes

import cats.effect.Sync
import org.http4s._
import com.uzo.shoppingcart.algebras.Categories
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

//implicit for EntityEncoder[F, List[Bran]]
import com.uzo.shoppingcart.http.json._


final class CategoryRoutes[F[_]: Sync](
                                        categories: Categories[F]
                                      ) extends Http4sDsl[F] {
  private[routes] val prefixPath = "/categories"
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok(categories.findAll)
  }
  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}