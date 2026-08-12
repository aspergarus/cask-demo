package entities

import scalasql.Sc

case class Product[T[_]](
  id: T[Int],
  name: T[String],
  description: T[String],
  price: T[Int],
  priceUnit: T[String],
)

object Product extends scalasql.Table[Product] {
  override def tableName = "products"

  given todoRW: upickle.default.ReadWriter[Product[Sc]] =
    upickle.default.macroRW[Product[Sc]]
}