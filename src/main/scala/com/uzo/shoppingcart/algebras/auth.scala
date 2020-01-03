package com.uzo.shoppingcart.algebras

import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion.User
import com.uzo.shoppingcart.domain.auth.{Password, UserName}
import dev.profunktor.auth.jwt.JwtToken


/**
  * There are also the authentication endpoints. We are going to use JSON Web Tokens (JWT)
  *  as the authentication methods, as we will further expand in Chapter 5.
  *  But unitl we get there, we can sketch something out with what we currently have and make some modifications later
  *  on if necessary.
  *
  * @tparam F
  */
trait Auth[F[_]]{
  def findUser(token: JwtToken): F[Option[User]]
  def newUser(username: UserName, password: Password): F[JwtToken]
  def login(username: UserName, password: Password): F[JwtToken]
  def logout(token: JwtToken, username: UserName): F[Unit]
}