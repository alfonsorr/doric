package doric

import cats.implicits._

import org.apache.spark.sql.Column

object DoricColumnPrivateAPI {
  implicit private[doric] class CollectionMethods(a: List[DoricColumn[_]]) {
    def mapDC[T](f: List[Column] => Column): DoricColumn[T] =
      a.traverse(_.elem).map(f).toDC
  }

  implicit private[doric] class Tuple2Methods(
      a: (DoricColumn[_], DoricColumn[_])
  ) {
    def mapNDC[T](f: (Column, Column) => Column): DoricColumn[T] =
      (a._1.elem, a._2.elem).mapN(f).toDC
  }

  implicit private[doric] class Tuple3Methods(
      a: (DoricColumn[_], DoricColumn[_], DoricColumn[_])
  ) {
    def mapNDC[T](f: (Column, Column, Column) => Column): DoricColumn[T] =
      (a._1.elem, a._2.elem, a._3.elem).mapN(f).toDC
  }

  implicit private[doric] class Tuple4Methods(
      a: (DoricColumn[_], DoricColumn[_], DoricColumn[_], DoricColumn[_])
  ) {
    def mapNDC[T](
        f: (Column, Column, Column, Column) => Column
    ): DoricColumn[T] =
      (a._1.elem, a._2.elem, a._3.elem, a._4.elem).mapN(f).toDC
  }

  implicit private[doric] class Tuple5Methods(
      a: (
          DoricColumn[_],
          DoricColumn[_],
          DoricColumn[_],
          DoricColumn[_],
          DoricColumn[_]
      )
  ) {
    def mapNDC[T](
        f: (Column, Column, Column, Column, Column) => Column
    ): DoricColumn[T] =
      (a._1.elem, a._2.elem, a._3.elem, a._4.elem, a._5.elem).mapN(f).toDC
  }
}
