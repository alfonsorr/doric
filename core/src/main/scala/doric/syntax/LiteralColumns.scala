package doric
package syntax

import doric.types.SparkType

trait LiteralColumns {

  implicit class LiteralOps[T](literal: NamedDoricColumn[T]) {
    def keepName[O: SparkType](
        f: DoricColumn[T] => DoricColumn[O]
    ): NamedDoricColumn[O] =
      f(literal).as(literal.columnName)
  }

}
