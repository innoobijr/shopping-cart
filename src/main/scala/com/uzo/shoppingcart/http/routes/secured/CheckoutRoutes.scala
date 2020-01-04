package com.uzo.shoppingcart.http.routes.secured

import cats.effect.Sync
import cats.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import com.uzo.shoppingcart.http.auth.users.CommonUser
import com.uzo.shoppingcart.domain.cart._
import com.uzo.shoppingcart.domain.checkout._
import com.uzo.shoppingcart.domain.order._
import com.uzo.shoppingcart.http.decoder._
import com.uzo.shoppingcart.http.json._
import com.uzo.shoppingcart.programs.CheckoutProgram

final class CheckoutRoutes[F[_]: Sync](
                                      program: CheckoutProgram[F]
                                      ) extends Http4sDsl[F]{

  private[routes] val prefixPath = "/checkout"

  private val httpsRoutes: AuthedRoutes[CommonUser, F] =
    AuthedRoutes.of{
      case ar @ POST -> Root as user =>
        ar.req.decodeR[Card]{ card =>
          program
            .checkout(user.value.id, card)
            .flatMap(Created(_))
            .recoverWith{
              case CartNotFound(userId) =>
                NotFound(s"Cart not found for user: ${userId.value}")
              case EmptyCartError =>
                BadRequest("Shoppign cart is empty!")
              case PaymentError(cause) =>
                BadRequest(cause)
              case OrderError(cause) =>
                BadRequest(cause)
            }
        }
    }

  def routes(
              authMiddleware: AuthMiddleware[F, CommonUser]
            ): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}

/**
  * Using recoverWith providied byt eh ApplicativeError instance we have in scope, we can recover
  * from bsuioess eroores and return the appropriate response. In the last chapter, we are going to modify it
  * to use classy prisms for error handling,
  *
  * Another new function to notice is `decodeR` which is a custom decoding function
  * that deals with validation errors from Refined and returns a response code 400 (Bad Request)
  * along with an error message instead of the defualt response code 422 (Unprocessable Entity) when there is an invalid
  * input such as an empty name. Find its definition below.
  */
