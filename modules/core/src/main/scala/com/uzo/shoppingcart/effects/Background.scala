package com.uzo.shoppingcart.effects

import cats.effect._
import cats.effect.implicits._
import cats.implicits._
import scala.concurrent.duration.FiniteDuration


/**
  *  Lets us schedule tasks to run in the background some time in the future.
  *
  *  WE could have done this directly using `Concurrent` and `Timer`; in fact this is what our implementation does,
  *  though, there are a fiew reason why having a custome interface is better:
  *   * We gain more control by restrciting what the final user can do
  *   * We aviod having `Concurrent` as a constraint, which allows arbirary effects
  *   * We acheive better testability, as we will see in Chapter 7
  * @tparam F
  */

// Interface
trait Background[F[_]]{
  def schedule[A](
                 fa: F[A],
                 duration: FiniteDuration
                 ): F[Unit]
}


object Background {
  def apply[F[_]](implicit ev: Background[F]): Background[F] = ev

  // Instance
  implicit def concurrentBackground[F[_] : Concurrent: Timer]: Background[F] =
    new Background[F] {
      override def schedule[A](fa: F[A], duration: FiniteDuration): F[Unit] =
        (Timer[F].sleep(duration) *> fa).start.void
    }

}
