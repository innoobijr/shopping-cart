package com.uzo.shoppingcart.http.routes

import java.time.Year

import cats.effect.Sync
import org.http4s._
import com.uzo.shoppingcart.algebras.Items
import com.uzo.shoppingcart.domain.brand
import com.uzo.shoppingcart.domain.brand._
import eu.timepit.refined.types.string
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

//implicit for EntityEncoder[F, List[Bran]]
import com.uzo.shoppingcart.http.json._
import com.uzo.shoppingcart.http.params._

final class ItemRoutes[F[_]: Sync](
                                    items: Items[F]
                                  ) extends Http4sDsl[F] {
  private[routes] val prefixPath = "/items"

  object BrandQueryParam extends OptionalQueryParamDecoderMatcher[BrandParam]("brand")

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? BrandQueryParam(brand) =>
      Ok(brand.fold(items.findAll)(b => items.findBy(b.toDomain)))
  }
  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}