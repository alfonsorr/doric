package doric.sem

import org.apache.spark.sql.SparkSession

object ChildColumnNotFound {
  def apply(expectedCol: String, foundCols: List[String])(implicit
      location: Location,
      sparkSession: SparkSession
  ): SparkErrorWrapper = {
    SparkErrorWrapper(
      new Throwable(
          s"[FIELD_NOT_FOUND] No such struct field `$expectedCol` in ${foundCols
              .mkString("`", "`, `", "`")}. SQLSTATE: 42704"
      )
    )
  }
}
