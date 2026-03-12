package doric.sem

import org.apache.spark.sql.SparkSession

object ColumnNotFound {
  def apply(expectedCol: String, foundCols: List[String])(implicit
      location: Location,
      sparkSession: SparkSession
  ): SparkErrorWrapper = {

    SparkErrorWrapper(
      new Throwable(
          s"[UNRESOLVED_COLUMN.WITH_SUGGESTION] A column, variable, or function parameter with name `$expectedCol` cannot be resolved. Did you mean one of the following? [${foundCols
              .mkString("`", "`, `", "`")}]. SQLSTATE: 42703"
      )
    )
  }
}
