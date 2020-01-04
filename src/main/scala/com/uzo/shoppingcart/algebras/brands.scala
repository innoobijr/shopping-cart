package com.uzo.shoppingcart.algebras

import com.uzo.shoppingcart.domain.brand.{Brand, BrandName}


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