package doric
package syntax

import doric.types.{Casting, SparkType}
import doric.DoricColumnPrivateAPI._

import org.apache.spark.sql.functions.{when => sparkWhen}

final private[doric] case class WhenBuilder[T](
    private[doric] val cases: Vector[(BooleanColumn, DoricColumn[T])] =
      Vector.empty
) {

  /**
    * Marks the rest of cases as null values of the provided type
    *
    * @param dt
    * Type class for spark data type
    * @return
    *   The doric column with the provided logic in the builder
    */
  def otherwiseNull(implicit
      dt: SparkType[T],
      C: Casting[Null, T]
  ): DoricColumn[T] =
    if (cases.isEmpty) {
      lit(null).cast[T]
    } else
      casesToWhenColumn

  /**
    * ads a case that if the condition is matched, the value is returned
    * @param cond
    *   BooleanColumn with the condition to satisfy
    * @param elem
    *   the returned element if the condition is true
    * @return
    *   new instance of the builder with the previous cases added
    */
  def caseW(cond: BooleanColumn, elem: DoricColumn[T]): WhenBuilder[T] =
    WhenBuilder(cases.:+((cond, elem)))

  private def casesToWhenColumn[TAux]: DoricColumn[TAux] = {
    val first = cases.head
    cases.tail.foldLeft(
      (first._1, first._2).mapNDC[TAux]((c, a) => sparkWhen(c, a))
    )((acc, c) =>
      (acc, c._1, c._2).mapNDC((a, cond, algo) => a.when(cond, algo))
    )
  }

  /**
    * For the rest of cases adds a default value
    * @param other
    *   the default value to return
    * @return
    *   The doric column with the provided logic in the builder
    */
  def otherwise(other: DoricColumn[T]): DoricColumn[T] =
    if (cases.isEmpty) other
    else (casesToWhenColumn, other).mapNDC(_.otherwise(_))

}
