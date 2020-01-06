package com.uzo.shoppingcart.http.routes

import cats.effect.Sync
import org.http4s._
import com.uzo.shoppingcart.algebras.Items
import com.uzo.shoppingcart.domain.brand._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import com.uzo.shoppingcart.http.params._
import com.uzo.shoppingcart.http.json._

//implicit for EntityEncoder[F, List[Bran]]

final class ItemRoutes[F[_]: Sync](
    items: Items[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/items"

  object BrandQueryParam extends OptionalQueryParamDecoderMatcher[BrandParam]("brand")

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root :? BrandQueryParam(brand) =>
      Ok(brand.fold(this.items.findAll)(b => items.findBy(b.toDomain)))

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}