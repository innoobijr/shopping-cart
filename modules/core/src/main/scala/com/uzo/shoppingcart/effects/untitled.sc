import java.util.UUID

import io.estatico.newtype.ops._
import io.estatico.newtype.NewType
import io.estatico.newtype.macros.newtype

object test {

  object Nat extends NewType.Of[Int] {
    def apply(n: Int): Option[Type] = if (n < 0) None else Some(wrap(n))
  }

  //val j = Point(1, 2)
  //(8, 9).coerce[Point]
  val s = Nat(8)
  print(s.get)
  //val h = UUID.randomUUID().coerce[BrandId]
}
