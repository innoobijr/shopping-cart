package com.uzo.shoppingcart.effects

import cats.{ ApplicativeError, MonadError }
import cats.effect.Bracket
import cats.mtl.ApplicativeAsk // {** WIT **}

package object effects {


  type MonadThrow[F[_]] = MonadError[F, Throwable]

  object MonadThrow {
    def apply[F[_]](implicit ev: MonadError[F, Throwable]): MonadThrow[F] = ev
  }

}
