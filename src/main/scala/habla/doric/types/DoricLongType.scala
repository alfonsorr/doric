package habla.doric
package types

import habla.doric.syntax.NumericOperations

import org.apache.spark.sql.types.{DataType, LongType}
import org.apache.spark.sql.Column

trait DoricLongType {

  type LongColumn = DoricColumn[Long]

  implicit val fromLong: SparkType[Long] = new SparkType[Long] {

    override def dataType: DataType = LongType

  }

  implicit val longArith: NumericOperations[Long] =
    new NumericOperations[Long] {}

  implicit val longCastToString: Casting[Long, String] =
    new SparkCasting[Long, String] {}

  implicit val longCastToFloat: Casting[Long, Float] =
    new SparkCasting[Long, Float] {}

  implicit val longCastToDouble: Casting[Long, Double] =
    new SparkCasting[Long, Double] {}

  implicit val longCastTInt: UnsafeCasting[Long, Int] =
    new SparkUnsafeCasting[Long, Int] {}

  object LongColumn {

    def apply(litv: Long): LongColumn =
      litv.lit

    def unapply(column: Column): Option[LongColumn] =
      DoricColumnExtr.unapply[Long](column)

  }
}
