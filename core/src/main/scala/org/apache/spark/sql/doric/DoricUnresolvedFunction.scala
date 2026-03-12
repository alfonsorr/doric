package org.apache.spark.sql.doric

import org.apache.spark.sql.catalyst.trees.CurrentOrigin.withOrigin
import org.apache.spark.sql.internal.{ColumnNode, UnresolvedFunction}
import org.apache.spark.sql.{Column, internal}

object DoricUnresolvedFunction {
  def fn(name: String, inputs: Column*): Column = {
    fn(name, isDistinct = false, inputs: _*)
  }

  def fn(name: String, isDistinct: Boolean, inputs: Column*): Column = {
    fn(name, isDistinct = isDistinct, isInternal = false, inputs)
  }

  def internalFn(name: String, inputs: Column*): Column = {
    fn(name, isDistinct = false, isInternal = true, inputs)
  }

  private def fn(
      name: String,
      isDistinct: Boolean,
      isInternal: Boolean,
      inputs: Seq[Column]
  ): Column = withOrigin {
    Column(
      internal.UnresolvedFunction(
        name,
        inputs.map(_.node),
        isDistinct = isDistinct,
        isInternal = isInternal
      )
    )
  }

  def unapply(un: UnresolvedFunction): Option[Seq[ColumnNode]] = Some(un.arguments)
}
