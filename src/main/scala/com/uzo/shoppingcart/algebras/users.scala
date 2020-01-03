package com.uzo.shoppingcart.algebras

import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion.User
import com.uzo.shoppingcart.domain.auth.{Password, UserId, UserName}


trait Users[F[_]]{
  def find(
          username: UserName,
          password: Password
          ): F[Option[User]]
  def create(
            userName: UserName,
            password: Password
            ): F[UserId]
}
