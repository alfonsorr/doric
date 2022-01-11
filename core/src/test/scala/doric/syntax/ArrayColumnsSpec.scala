package doric
package syntax

import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers

class ArrayColumnsSpec
    extends DoricTestElements
    with EitherValues
    with Matchers {

  import spark.implicits._

  describe("ArrayOps") {
    val result     = "result"
    val testColumn = "col"
    it("should extract a index") {
      val df = List((List(1, 2, 3), 1))
        .toDF(testColumn, "something")
        .select("col")
      df.withColumn(result, colArray[Int](testColumn).getIndex(1))
        .select(result)
        .as[Int]
        .head() shouldBe 2
    }

    it(
      "should transform the elements of the array with the provided function"
    ) {
      val df = List((List(1, 2, 3), 7)).toDF(testColumn, "something")
      df.withColumn(
        result,
        colArrayInt(testColumn).transform(_ + colInt("something"))
      ).select(result)
        .as[List[Int]]
        .head() shouldBe List(8, 9, 10)
    }

    it("should capture the error if anything in the lambda is wrong") {
      val df = List((List(1, 2, 3), 7)).toDF(testColumn, "something")
      colArrayInt("col")
        .transform(_ + colInt("something2"))
        .elem
        .run(df)
        .toEither
        .left
        .value
        .head
        .message shouldBe "Cannot resolve column name \"something2\" among (col, something)"

      colArrayInt("col")
        .transform(_ => colString("something"))
        .elem
        .run(df)
        .toEither
        .left
        .value
        .head
        .message shouldBe "The column with name 'something' is of type IntegerType and it was expected to be StringType"
    }

    it(
      "should transform with index the elements of the array with the provided function"
    ) {
      val df =
        List((List(10, 20, 30), 7))
          .toDF(testColumn, "something")
          .select("col")
      df.withColumn(result, colArrayInt(testColumn).transformWithIndex(_ + _))
        .select(result)
        .as[List[Int]]
        .head() shouldBe List(10, 21, 32)
    }

    it("should capture errors in transform with index") {
      val df = List((List(10, 20, 30), "7")).toDF(testColumn, "something")
      colArrayInt(testColumn)
        .transformWithIndex(_ + _ + colInt("something"))
        .elem
        .run(df)
        .toEither
        .left
        .value
        .head
        .message shouldBe "The column with name 'something' is of type StringType and it was expected to be IntegerType"

      colArrayInt(testColumn)
        .transformWithIndex(_ + _ + colInt("something2"))
        .elem
        .run(df)
        .toEither
        .left
        .value
        .head
        .message shouldBe "Cannot resolve column name \"something2\" among (col, something)"
    }

    it(
      "should aggregate the elements of the array with the provided function"
    ) {
      val df =
        List((List(10, 20, 30), 7))
          .toDF(testColumn, "something")
          .select("col")
      df.withColumn(
        result,
        colArrayInt(testColumn).aggregate[Int](100.lit)(_ + _)
      ).select(result)
        .as[Int]
        .head() shouldBe 160
    }

    it("should capture errors in aggregate") {
      val df = List((List(10, 20, 30), "7")).toDF(testColumn, "something")
      val errors = colArrayInt(testColumn)
        .aggregate(colInt("something2"))(_ + _ + colInt("something"))
        .elem
        .run(df)
        .toEither
        .left
        .value

      errors.toChain.size shouldBe 2
      errors.map(_.message).toChain.toList shouldBe List(
        "Cannot resolve column name \"something2\" among (col, something)",
        "The column with name 'something' is of type StringType and it was expected to be IntegerType"
      )
    }

    it(
      "should aggregate the elements of the array with the provided function with final transform"
    ) {
      val df = List((List(10, 20, 30), 7)).toDF(testColumn, "something")
      df.withColumn(
        result,
        colArrayInt(testColumn)
          .aggregateWT[Int, String](100.lit)(
            _ + _,
            x => (x + col[Int]("something")).cast
          )
      ).select(result)
        .as[String]
        .head() shouldBe "167"
    }

    it("should capture errors in aggregate with final transform") {
      val df = List((List(10, 20, 30), "7")).toDF(testColumn, "something")
      val errors = colArrayInt(testColumn)
        .aggregateWT[Int, String](colInt("something2"))(
          _ + _ + colInt("something"),
          x => (x + colInt("something3")).cast
        )
        .elem
        .run(df)
        .toEither
        .left
        .value

      errors.toChain.size shouldBe 3
      errors.map(_.message).toChain.toList shouldBe List(
        "Cannot resolve column name \"something2\" among (col, something)",
        "The column with name 'something' is of type StringType and it was expected to be IntegerType",
        "Cannot resolve column name \"something3\" among (col, something)"
      )
    }

    it("should filter") {
      List((List(10, 20, 30), 25))
        .toDF(testColumn, "val")
        .withColumn(result, colArrayInt("col").filter(_ < c"val"))
        .select(result)
        .as[List[Int]]
        .head() shouldBe List(10, 20)
    }
  }

}
