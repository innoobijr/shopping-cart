package com.uzo.shoppingcart.algebras

import cats.effect._
import cats.implicits._
import com.uzo.shoppingcart.domain.brand._
import com.uzo.shoppingcart.effects._
import skunk._
import skunk.codec.all._
import skunk.implicits._
import skunk.util.Origin

/**
  * Our Brand domain consists of two requests, a GET to retrieve the list
  * of brands and a POST to create new brands. The POST equest should only
  * be used by administrators, however, we dont consider permission details at
  * the algebra level.
  */
trait Brands[F[_]]{
  def findAll: F[List[Brand]]
  def create(brand: BrandName): F[Unit]
}

object LiveBrands {
  def make[F[_]: Sync](
      sessionPool: Resource[F, Session[F]]
  ): F[Brands[F]] =
    Sync[F].delay(
      new LiveBrands[F](sessionPool)
    )
}

final class LiveBrands[F[_]: BracketThrow: GenUUID] private (
    sessionPool: Resource[F, Session[F]]
) extends Brands[F] {
  import BrandQueries._


  def findAll: F[List[Brand]] =
    sessionPool.use(_.execute(selectAll))

  def create(name: BrandName): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertBrand).use { cmd =>
        GenUUID[F].make[BrandId].flatMap { id =>
          cmd.execute(Brand(id, name)).void
        }
      }
    }
}

private object BrandQueries {

  val codec: Codec[Brand] =
    (uuid ~ varchar).imap {
      case i ~ n =>
        Brand(
          BrandId(i),
          BrandName(n)
        )
    }(b => b.uuid.value ~ b.name.value)

  val selectAll: Query[Void, Brand] =
    Query("""
        SELECT * FROM brands
       """, Origin.unknown, Void.codec.asEncoder, codec.asDecoder)

  val insertBrand: Command[Brand] =
    Command(s"""
        INSERT INTO brands
        VALUES ($codec)
        """,Origin.unknown, codec.asEncoder)

}