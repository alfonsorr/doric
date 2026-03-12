package doric
package syntax

import cats.implicits._
import doric.DoricColumn.sparkFunction

import org.apache.spark.sql.{functions => f}

protected trait BooleanColumns {

  /**
    * Inversion of boolean expression, i.e. NOT.
    *
    * @group Boolean Type
    * @see [[org.apache.spark.sql.functions.not]]
    */
  def not(col: BooleanColumn): BooleanColumn = col.elem.map(f.not).toDC

  implicit class BooleanOperationsSyntax(
      column: DoricColumn[Boolean]
  ) {

    /**
      * Inversion of boolean expression, i.e. NOT.
      *
      * @group Boolean Type
      * @see [[not]]
      */
    def unary_! : DoricColumn[Boolean] = not(column)

    /**
      * Boolean AND
      *
      * @group Boolean Type
      */
    def and(other: DoricColumn[Boolean]): DoricColumn[Boolean] =
      sparkFunction(column, other, _ && _)

    /**
      * Boolean AND
      *
      * @group Boolean Type
      */
    def &&(other: DoricColumn[Boolean]): DoricColumn[Boolean] =
      and(other)

    /**
      * Boolean OR
      *
      * @group Boolean Type
      */
    def or(other: DoricColumn[Boolean]): DoricColumn[Boolean] =
      sparkFunction(column, other, _ || _)

    /**
      * Boolean OR
      *
      * @group Boolean Type
      */
    def ||(other: DoricColumn[Boolean]): DoricColumn[Boolean] =
      or(other)

    /**
      * Returns null if the condition is true, and throws an exception otherwise.
      *
      * @throws java.lang.RuntimeException if the condition is false
      * @group Boolean Type
      * @see [[org.apache.spark.sql.functions.assert_true(c:org\.apache\.spark\.sql\.Column):* org.apache.spark.sql.functions.assert_true]]
      */
    def assertTrue: NullColumn = column.elem.map(f.assert_true).toDC

    /**
      * Returns null if the condition is true; throws an exception with the error message otherwise.
      *
      * @throws java.lang.RuntimeException if the condition is false
      * @group Boolean Type
      * @see [[org.apache.spark.sql.functions.assert_true(c:org\.apache\.spark\.sql\.Column,e:* org.apache.spark.sql.functions.assert_true]]
      */
    def assertTrue(msg: StringColumn): NullColumn =
      (column.elem, msg.elem).mapN(f.assert_true).toDC
  }
}
