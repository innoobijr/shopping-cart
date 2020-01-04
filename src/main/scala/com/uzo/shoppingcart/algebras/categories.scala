package com.uzo.shoppingcart.algebras

import com.uzo.shoppingcart.domain.category._

trait Categories[F[_]]{
  def findAll: F[List[Category]]
  def create(name: CategoryName): F[Unit]
}