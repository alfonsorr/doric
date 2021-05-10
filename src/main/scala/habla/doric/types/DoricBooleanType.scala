package habla.doric
package types

import org.apache.spark.sql.types.{BooleanType, DataType}
import org.apache.spark.sql.Column

trait DoricBooleanType {

  type BooleanColumn = DoricColumn[Boolean]

  object BooleanColumn {

    def apply(litv: Boolean): BooleanColumn =
      litv.lit

    def unapply(column: Column): Option[BooleanColumn] =
      DoricColumnExtr.unapply[Boolean](column)
  }

  implicit val fromBoolean: FromDf[Boolean] = new FromDf[Boolean] {

    override def dataType: DataType = BooleanType
  }

}