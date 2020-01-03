package com.uzo.shoppingcart.algebras

import com.uzo.shoppingcart.domain.category.{Category, CategoryName}

trait Categories[F[_]]{
  def findAll: F[List[Category]]
  def crate(name: CategoryName): F[Unit]
}