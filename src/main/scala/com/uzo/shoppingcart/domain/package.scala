package com.uzo.shoppingcart

import cats.Eq
import cats.implicits._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import java.util.UUID

package object domain {
  // Kind projects
  // https://underscore.io/blog/posts/2016/12/05/type-lambdas.html
  private def coercibleEq[A: Eq, B: Coercible[A, ?]]: Eq[B] =
    new Eq[B]{
      def eqv(x: B, y: B): Boolean =
        Eq[A].eqv(x.repr.asInstanceOf[A], y.repr.asInstanceOf[A])
    }

  implicit def coercibleStringEq[B: Coercible[String, ?]]: Eq[B] =
    coercibleEq[String, B]

  implicit def coercibleUuidEq[B: Coercible[UUID, ?]]: Eq[B] =
    coercibleEq[UUID, B]

  implicit def coercibleIntEq[B: Coercible[Int, ?]]: Eq[B] =
    coercibleEq[Int, B]

}
