package com.uzo.shoppingcart.domain

import io.estatico.newtype.macros.newtype
import com.uzo.shoppingcart.domain.item._
import java.util.UUID

import com.uzo.shoppingcart.domain.auth.UserId

import scala.util.control.NoStackTrace
//import com.uzo.shoppingcart.domain.auth.UserId
import squants.market.Money

object cart {
  @newtype case class Quantity(value:Int)
  @newtype case class Cart(items: Map[ItemId, Quantity])
  @newtype case class CartId(value: UUID)

  case class CartItem(item: Item, quantity: Quantity)
  case class CartTotal(items: List[CartItem], total: Money)
  case class CartNotFound(userId: UserId) extends NoStackTrace
}
