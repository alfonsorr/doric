package doric
package syntax

import cats.implicits._
import org.apache.spark.sql.doric.{DoricUnresolvedFunction => df}
import org.apache.spark.sql.{functions => f}

protected trait NumericColumns32 {

  /**
    * INTEGRAL OPERATIONS
    */
  implicit class IntegralOperationsSyntax32[T: IntegralType](
      column: DoricColumn[T]
  ) {

    /**
      * Shift the given value numBits left.
      *
      * group Numeric Type
      * @see [[org.apache.spark.sql.functions.shiftleft]]
      */
    def shiftLeft(numBits: IntegerColumn): DoricColumn[T] =
      (column.elem, numBits.elem)
        .mapN((c, n) => df.fn("shiftleft", c, n))
        .toDC

    /**
      * (Signed) shift the given value numBits right.
      *
      * group Numeric Type
      * @see [[org.apache.spark.sql.functions.shiftright]]
      */
    def shiftRight(numBits: IntegerColumn): DoricColumn[T] =
      (column.elem, numBits.elem)
        .mapN((c, n) => df.fn("shiftright", c, n))
        .toDC

    /**
      * Unsigned shift the given value numBits right.
      *
      * group Numeric Type
      * @see [[org.apache.spark.sql.functions.shiftrightunsigned]]
      */
    def shiftRightUnsigned(numBits: IntegerColumn): DoricColumn[T] =
      (column.elem, numBits.elem)
        .mapN((c, n) => df.fn("shiftrightunsigned", c, n))
        .toDC

    /**
      * Computes bitwise NOT (~) of a number.
      *
      * @group Numeric Type
      * @see [[org.apache.spark.sql.functions.bitwise_not]]
      */
    def bitwiseNot: DoricColumn[T] = column.elem.map(f.bitwise_not).toDC
  }

}
