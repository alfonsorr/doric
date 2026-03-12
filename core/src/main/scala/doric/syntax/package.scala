package doric

import cats.implicits._
import doric.types.SparkType
import org.apache.spark.sql.catalyst.expressions.{ExprId, Expression, LambdaFunction, NamedExpression, NamedLambdaVariable}
import org.apache.spark.sql.doric.{DoricUnresolvedFunction => df}
import org.apache.spark.sql.{Column, internal, functions => f}

import java.util.concurrent.atomic.AtomicReference

package object syntax {

  /**
    * Abstract method for Array Columns and Map Columns
    *
    * Returns element of array at given index in value if column is array. Returns value for
    * the given key in value if column is map.
    *
    * @param dc doric column where the item is to be searched
    * @param key doric column
    * @tparam T type of the doric column where the item is to be searched
    * @tparam K type of "key" doric column to perform the search
    * @tparam V type of "value" doric column result
    */
  @inline def elementAtAbstract[T, K, V](
      dc: DoricColumn[T],
      key: DoricColumn[K]
  ): DoricColumn[V] = {
    (dc.elem, key.elem)
      .mapN((c, k) => df.fn("element_at", c, k))
      .toDC
  }

  /**
    * Abstract method for Array Columns and String Columns
    *
    * Returns a reversed string or an array with reverse order of elements.
    *
    * @param dc doric column to be reversed
    * @tparam T type of doric column (string or array)
    */
  @inline def reverseAbstract[T](
      dc: DoricColumn[T]
  ): DoricColumn[T] =
    dc.elem.map(f.reverse).toDC

  @inline private[syntax] def value[A: SparkType](
      name: String
  ): DoricColumn[A] = {
    val exprId: ExprId              = NamedExpression.newExprId
    val value: AtomicReference[Any] = new AtomicReference()
    DoricColumn(
      new Column(
        internal.UnresolvedNamedLambdaVariable(name)
      )
    )
  }

  /*private[syntax] def createLambda(function: Column, x: Column*): Column = {
    Column(internal.LambdaFunction(function.node, x.map(_.node.asInstanceOf[UnresolvedNamedLambdaVariable])))
  }*/

  @inline private[syntax] def x[A: SparkType]: DoricColumn[A] =
    value("x")
  @inline private[syntax] def y[A: SparkType]: DoricColumn[A] =
    value("y")
  @inline private[syntax] def z[A: SparkType]: DoricColumn[A] =
    value("z")

  @inline private[syntax] def lam1(
      e: Expression,
      xarg: Expression
  ): LambdaFunction =
    LambdaFunction(e, Seq(xarg.asInstanceOf[NamedLambdaVariable]))
  @inline private[syntax] def lam2(
      e: Expression,
      xarg: Expression,
      yarg: Expression
  ): LambdaFunction =
    LambdaFunction(
      e,
      Seq(
        xarg.asInstanceOf[NamedLambdaVariable],
        yarg.asInstanceOf[NamedLambdaVariable]
      )
    )
  @inline private[syntax] def lam3(
      e: Expression,
      xarg: Expression,
      yarg: Expression,
      zarg: Expression
  ): LambdaFunction =
    LambdaFunction(
      e,
      Seq(
        xarg.asInstanceOf[NamedLambdaVariable],
        yarg.asInstanceOf[NamedLambdaVariable],
        zarg.asInstanceOf[NamedLambdaVariable]
      )
    )
}
