package doric

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

import java.util.TimeZone

trait SparkSessionTestWrapper {

  lazy implicit val spark: SparkSession = {
    Logger.getLogger("org").setLevel(Level.OFF)

    val timeZone: String = "UTC"
    TimeZone.setDefault(TimeZone.getTimeZone(timeZone))

    val ss = SparkSession
      .builder()
      .master("local")
      //.config("spark.driver.bindAddress", "127.0.0.1")
      .config("spark.sql.session.timeZone", timeZone)
      .config("spark.sql.datetime.java8API.enabled", value = true)
      .config("spark.sql.ansi.enabled", "false") // Disable ANSI mode for Spark 4.0 compatibility
      .appName("spark session")
      .getOrCreate()

    //ss.sparkContext.setLogLevel("ERROR")
    ss
  }

}
