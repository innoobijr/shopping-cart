package com.uzo.shoppingcart.algebras


import cats.effect._
import cats.implicits._
import com.uzo.shoppingcart.domain.category._
import com.uzo.shoppingcart.effects._
import skunk._
import skunk.codec.all._
import skunk.implicits._
import com.uzo.shoppingcart.domain.category._

trait Categories[F[_]]{
  def findAll: F[List[Category]]
  def create(name: CategoryName): F[Unit]
}

object LiveCategories {
  def make[F[_]: Sync](
                        sessionPool: Resource[F, Session[F]]
                      ): F[Categories[F]] =
    Sync[F].delay(
      new LiveCategories[F](sessionPool)
    )
}

final class LiveCategories[F[_]: BracketThrow: GenUUID] private (
                                                                  sessionPool: Resource[F, Session[F]]
                                                                ) extends Categories[F] {
  import CategoryQueries._

  def findAll: F[List[Category]] =
    sessionPool.use(_.execute(selectAll))

  def create(name: CategoryName): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertCategory).use { cmd =>
        GenUUID[F].make[CategoryId].flatMap { id =>
          cmd.execute(Category(id, name)).void
        }
      }
    }

}

private object CategoryQueries {

  val codec: Codec[Category] = (uuid ~ varchar).imap {
    case i ~ n =>
      Category(
        CategoryId(i),
        CategoryName(n)
      )
  }(c => c.uuid.value ~ c.name.value)

  val selectAll: Query[Void, Category] =
    sql"""SELECT * FROM categories""".query(codec)

  val insertCategory: Command[Category] =
    sql"""
        INSERT INTO categories
        VALUES ($codec)
        """.command

}