package com.uzo.shoppingcart.domain

import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import java.util.UUID
import javax.crypto.Cipher
import scala.util.control.NoStackTrace

object auth {
  @newtype case class UserId(value: UUID)
  @newtype case class UserName(value: String)
  @newtype case class Password(value: String)

  @newtype case class EncryptPassword(value: String)

  @newtype case class EncruptCipher(value: Cipher)
  @newtype case class DecryptCipher(value: Cipher)

  //------------------ user registration --------------------------

  @newtype case class UserNameParam(value: NonEmptyString){
    def toDomain: UserName = UserName(value.value.toLowerCase)
  }

  @newtype case class PasswordParam(value: NonEmptyString){
    def toDomain: Password = Password(value.value)
  }

  case class CreateUser(
    username: UserNameParam,
    passwod: PasswordParam
                       )

  case class UserNameInUser(username: UserName) extends NoStackTrace
  case class InvalidUserOrPassword(username: UserName) extends NoStackTrace
  case object UnsupportedOperated extends NoStackTrace

  case object TokenNotFound extends NoStackTrace

  // --------------- user login -----------------
  case class LoginUser(
                      username: UserNameParam,
                      password: PasswordParam
                      )


}
