package com.uzo.shoppingcart.http.routes

import cats.effect.Sync
import org.http4s._
import com.uzo.shoppingcart.algebras.Brands
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

//implicit for EntityEncoder[F, List[Bran]]
import com.uzo.shoppingcart.http.json._

/**
  * We are going to be representing routes using final classes with an abstact effect type
  * that has an instance of cats.effect.Sync, required by the HttpRoutes constructor, as
  * we will see soon.
  */
final class BrandRoutes[F[_] : Sync](
                                    brands: Brands[F]
                                    ) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/brands"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F]{
    case GET -> Root =>
      Ok(brands.findAll)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

  /**
    * There are a few things going on here:
    * * We have a `F[_]: Sync` required by HttpRoutes.of[F]
    * * We have the `Brands[F]` algebra as an argument to our class
    * * We extend `Http4sDsl[F]`
    * * There is a `prefixPath` made `private`, which indicates the root of our endpoint
    * * TFinal we ahv ea public routes which uses a Route that lets us add a `prefixPath` to a group of endpoints
    * denotes as `HttpRoute`
    *
    * Having a prefixPath and httpsRoutes as prviate functin is a prefernce.
    */


}
