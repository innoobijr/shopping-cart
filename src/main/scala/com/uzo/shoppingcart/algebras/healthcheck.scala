package com.uzo.shoppingcart.algebras

import cats.Parallel
import cats.effect._
import cats.effect.implicits._
import cats.implicits._
import dev.profunktor.redis4cats.algebra.RedisCommands
import scala.concurrent.duration._
import com.uzo.shoppingcart.domain.healthcheck._
import skunk._
import skunk.codec.all._
import skunk.implicits._

// Algebra
trait HealthCheck[F[_]]{
    def status: F[AppStatus]
}

// Smart Constructor
object LiveHealthCheck {
    def make[F[_]: Concurrent : Parallel: Timer](
        sessionPool: Resource[F, Session[F]],
        redis: RedisCommands[F, String, String]
    ): F[HealthCheck[F]] = 
    Sync[F].delay(
        new LiveHealthCheck[F](sessionPool, redis)
    )
}

// Intepreter
final class LiveHealthCheck[F[_]: Concurrent: Parallel: Timer] private(
    sessionPool: Resource[F, Session[F]],
    redis: RedisCommands[F, String, String]
) extends HealthCheck[F]{
    
    val q: Query[Void, Int] = 
        sql"SELECT pid FROM pg_stat_activity".query(int4)
    
    val redisHealth: F[RedisStatus] = 
        redis.ping
            .map(_.nonEmpty)
            .timeout(1.second)
            .orElse(false.pure[F])
            .map(RedisStatus.apply)
    
    val postgresHealth: F[PostgresStatus] = 
        sessionPool
            .use(_.execute(q))
            .map(_.nonEmpty)
            .orElse(false.pure[F])
            .map(PostgresStatus.apply)
    
    val status: F[AppStatus] = 
        (redisHealth, postgresHealth).parMapN(AppStatus)

}
