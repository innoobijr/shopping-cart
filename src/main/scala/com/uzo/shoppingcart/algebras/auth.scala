package com.uzo.shoppingcart.algebras

import cats._
import cats.effect.Sync
import cats.implicits._
import com.uzo.shoppingcart.domain.auth.{Password, UserName}
import com.uzo.shoppingcart.http.auth.users._
import dev.profunktor.redis4cats.algebra.RedisCommands
import dev.profunktor.auth.jwt.JwtToken
import pdi.jwt.JwtClaim
import com.uzo.shoppingcart.http.auth.users.CommonUser
import com.uzo.shoppingcart.http.auth.users.AdminUser
import com.uzo.shoppingcart.http.auth.users
import com.uzo.shoppingcart.http.json._
import com.uzo.shoppingcart.effects._
import io.circe.syntax._
import io.circe.parser.decode
import com.uzo.shoppingcart.config.data.TokenExpiration
import com.uzo.shoppingcart.domain.auth
import com.uzo.shoppingcart.domain.auth.UserNameInUse
import com.uzo.shoppingcart.domain.auth.InvalidUserOrPassword


/**
  * There are also the authentication endpoints. We are going to use JSON Web Tokens (JWT)
  *  as the authentication methods, as we will further expand in Chapter 5.
  *  But unitl we get there, we can sketch something out with what we currently have and make some modifications later
  *  on if necessary.
  *
  * @tparam F
  */
trait Auth[F[_]]{
  def newUser(username: UserName, password: Password): F[JwtToken]
  def login(username: UserName, password: Password): F[JwtToken]
  def logout(token: JwtToken, username: UserName): F[Unit]
}

trait Tokens[F[_]] {
  def create: F[JwtToken]
  }


// Specialized in retrieving a specific kind of user, indicated by second parameter A
trait UsersAuth[F[_], A]{
  def findUser(token: JwtToken)(claim: JwtClaim): F[Option[A]]
}

class LiveUsersAuth[F[_]: Functor](
  redis: RedisCommands[F, String, String]
) extends UsersAuth[F, CommonUser]{

  /**
   * Our function tried to find the user by token in Redis and if there is a result, it tried ot 
   * decode the JSON as the desired User type. A token is persisted as a simple key, with its value 
   * being the serialized user in JSON format.
   * 
   */ 
  def findUser(token: JwtToken)(claim: JwtClaim): F[Option[CommonUser]] = 
  redis
    .get(token.value)
    .map(_.flatMap{ u =>
      decode[User](u).toOption.map(CommonUser.apply)
    })
}

// Interpreter for AdminUser
class LiveAdminAuth[F[_]: Applicative](
  adminToken: JwtToken,
  adminUser: AdminUser
) extends UsersAuth[F, AdminUser]{

  /**
   * It compare the token with the unique AdminToken that has been passed ot the interpreter
   * on initialization, and in case of match, it returns the adminUser stored in memeory.
   */
  def findUser(token: JwtToken)(claim: JwtClaim): F[Option[users.AdminUser]] = 
    (token == adminToken)
      .guard[Option]
      .as(adminUser)
      .pure[F]
}
object LiveUsersAuth {
  def make[F[_]: Sync](
    redis: RedisCommands[F, String, String]
  ): F[UsersAuth[F, CommonUser]] = 
    Sync[F].delay(
      new LiveUsersAuth(redis)
    )
}

object LiveAdminAuth {
  def make[F[_]: Sync](
    adminToken: JwtToken,
    adminUser: AdminUser
  ): F[UsersAuth[F, AdminUser]] = 
  Sync[F].delay(
    new LiveAdminAuth(adminToken, adminUser)
  )
}

final class LiveAuth[F[_]: GenUUID: MonadThrow] private(
  tokenExpiration: TokenExpiration,
  tokens: Tokens[F],
  users: Users[F],
  redis: RedisCommands[F, String, String]
) extends Auth[F]{
  private val TokenExpiration = tokenExpiration.value

  /**
   * We try to find the user in PostGres. If it doesn exist, we proceed with its creation
   * otherwise, we raise a `UserNameInUserError`.
   *  Create a user means persisiting it in Postgres, create a JWT Token, serializing the user as 
   * JSON and persisting both the token and the serialized user in Redis for fast access, indicating
   * an expiration time. 
   */ 
  def newUser(username: auth.UserName, password: auth.Password): F[JwtToken] =
    users.find(username, password).flatMap{
      case Some(_) => UserNameInUse(username).raiseError[F, JwtToken]
      case None => 
        for {
          i <- users.create(username, password)
          t <- tokens.create
          u = User(i,username).asJson.noSpaces
          _ <- redis.setEx(t.value, u, TokenExpiration)
          _ <- redis.setEx(username.value, t.value, TokenExpiration)
        } yield t
    }
  
    def login(username: auth.UserName, password: auth.Password): F[JwtToken] =
      users.find(username, password).flatMap{
        case None => InvalidUserOrPassword(username).raiseError[F, JwtToken]
        case Some(user) =>
          redis.get(username.value).flatMap{
            case Some(t) => JwtToken(t).pure[F]
            case None =>
            tokens.create.flatTap { t =>
              redis.setEx(t.value, user.asJson.noSpaces, TokenExpiration) *>
              redis.setEx(username.value, t.value, TokenExpiration)
              }
          }
      }

    def logout(token: JwtToken, username: auth.UserName): F[Unit] = 
      redis.del(token.value) *> redis.del(username.value)
}
  //Smart constructor
  object LiveAuth {
      def make[F[_]: Sync](
        tokenExpiration: TokenExpiration,
        tokens: Tokens[F],
        users: Users[F],
        redis: RedisCommands[F, String, String]
      ): F[Auth[F]] = 
        Sync[F].delay(
          new LiveAuth(tokenExpiration, tokens, users, redis)
        )
  
}