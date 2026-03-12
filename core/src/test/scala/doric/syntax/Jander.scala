package doric.syntax

import org.apache.spark.sql.classic.SparkSession

class Jander {

  val ss = SparkSession.builder().getOrCreate()

  ss.emptyDataFrame.groupBy()

}
