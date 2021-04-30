package habla.doric
package syntax

import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers

import org.apache.spark.sql.types.{IntegerType, StringType}

case class User(name: String, surname: String, age: Int)

class DStructOpsSpec extends DoricTestElements with DStructOps with EitherValues with Matchers {

  import spark.implicits._

  private val df = List((User("John", "doe", 34), 1))
    .toDF("col", "delete")
    .select("col")

  describe("Dinamic struct column") {
    it("can get values subcolumns") {
      df.validateColumnType(getStruct("col").getChild[String]("name"))
      df.validateColumnType(getStruct("col").getChild[Int]("age"))
    }

    it("generates a error if the sub column doesn't exist") {
      getStruct("col")
        .getChild[String]("jander")
        .elem
        .run(df)
        .toEither
        .left
        .value
        .head shouldBe ChildColumnNotFound("jander", List("name", "surname", "age"))
    }

    it("throws an error if the sub column is not of the provided type") {
      getStruct("col")
        .getChild[String]("age")
        .elem
        .run(df)
        .toEither
        .left
        .value
        .head shouldBe ColumnTypeError("age", StringType, IntegerType)
    }
  }

}