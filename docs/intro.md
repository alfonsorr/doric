---
title: Doric Documentation
permalink: docs/
---

# Introduction to doric

Doric is very easy to work with, follow the [installation guide]({{ site.baseurl }}{% link docs/installation.md %})
first.

Then just import doric to your code


```scala
import doric._
```

If you have basic knowlege of spark you have to change the way to reference to columns, only adding the expected type of
the column.

```scala
val stringCol = col[String]("str")
// stringCol: DoricColumn[String] = DoricColumn(
//   Kleisli(doric.types.SparkType$$Lambda$2630/2087974216@34f2d3a6)
// )
```

And use this references as normal spark columns with the provided `select` and `withColumn` methods.

```scala
import spark.implicits._

val df = List("hi", "welcome", "to", "doric").toDF("str")
// df: DataFrame = [str: string]

df.select(stringCol).show()
// +-------+
// |    str|
// +-------+
// |     hi|
// |welcome|
// |     to|
// |  doric|
// +-------+
//
```

Doric adds a extra layer of knowledge to your column assigning a type, that is checked against the spark datatype in the
dataframe. As in the case of a dataframe that we ask the wrong column name, doric will also detect that the type is not
the expected.

```scala
val wrongName = col[String]("string")
df.select(wrongName)
// doric.sem.DoricMultiError: Found 1 error in select
//   Cannot resolve column name "string" among (str)
//   	located at . (intro.md:47)
// 
// 	at doric.sem.package$ErrorThrower.$anonfun$returnOrThrow$1(package.scala:9)
// 	at cats.data.Validated.fold(Validated.scala:29)
// 	at doric.sem.package$ErrorThrower.returnOrThrow(package.scala:9)
// 	at doric.sem.TransformOps$DataframeTransformationSyntax.select(TransformOps.scala:79)
// 	at repl.MdocSession$App$$anonfun$5.apply(intro.md:48)
// 	at repl.MdocSession$App$$anonfun$5.apply(intro.md:46)
// Caused by: org.apache.spark.sql.AnalysisException: Cannot resolve column name "string" among (str)
// 	at org.apache.spark.sql.Dataset.org$apache$spark$sql$Dataset$$resolveException(Dataset.scala:272)
// 	at org.apache.spark.sql.Dataset.$anonfun$resolve$1(Dataset.scala:263)
// 	at scala.Option.getOrElse(Option.scala:189)
// 	at org.apache.spark.sql.Dataset.resolve(Dataset.scala:263)
// 	at org.apache.spark.sql.Dataset.col(Dataset.scala:1359)
// 	at org.apache.spark.sql.Dataset.apply(Dataset.scala:1326)
// 	at doric.types.SparkType.$anonfun$validate$1(SparkType.scala:39)
// 	at cats.data.Kleisli.$anonfun$map$1(Kleisli.scala:40)
// 	at cats.data.Kleisli.$anonfun$map$1(Kleisli.scala:40)
// 	at cats.data.Kleisli.$anonfun$map$1(Kleisli.scala:40)
// 	at doric.sem.TransformOps$DataframeTransformationSyntax.select(TransformOps.scala:77)
// 	... 2 more
```

```scala
val wrongType = col[Int]("string")
df.select(wrongType)
// doric.sem.DoricMultiError: Found 1 error in select
//   Cannot resolve column name "string" among (str)
//   	located at . (intro.md:58)
// 
// 	at doric.sem.package$ErrorThrower.$anonfun$returnOrThrow$1(package.scala:9)
// 	at cats.data.Validated.fold(Validated.scala:29)
// 	at doric.sem.package$ErrorThrower.returnOrThrow(package.scala:9)
// 	at doric.sem.TransformOps$DataframeTransformationSyntax.select(TransformOps.scala:79)
// 	at repl.MdocSession$App$$anonfun$7.apply(intro.md:59)
// 	at repl.MdocSession$App$$anonfun$7.apply(intro.md:57)
// Caused by: org.apache.spark.sql.AnalysisException: Cannot resolve column name "string" among (str)
// 	at org.apache.spark.sql.Dataset.org$apache$spark$sql$Dataset$$resolveException(Dataset.scala:272)
// 	at org.apache.spark.sql.Dataset.$anonfun$resolve$1(Dataset.scala:263)
// 	at scala.Option.getOrElse(Option.scala:189)
// 	at org.apache.spark.sql.Dataset.resolve(Dataset.scala:263)
// 	at org.apache.spark.sql.Dataset.col(Dataset.scala:1359)
// 	at org.apache.spark.sql.Dataset.apply(Dataset.scala:1326)
// 	at doric.types.SparkType.$anonfun$validate$1(SparkType.scala:39)
// 	at cats.data.Kleisli.$anonfun$map$1(Kleisli.scala:40)
// 	at cats.data.Kleisli.$anonfun$map$1(Kleisli.scala:40)
// 	at cats.data.Kleisli.$anonfun$map$1(Kleisli.scala:40)
// 	at doric.sem.TransformOps$DataframeTransformationSyntax.select(TransformOps.scala:77)
// 	... 2 more
```
This type of errors are obtained in runtime, but now that we know the exact type of the column,
we can operate according to the type.

```scala
val concatCol = concat(stringCol, stringCol)
// concatCol: DoricColumn[String] = DoricColumn(
//   Kleisli(cats.data.Kleisli$$Lambda$2639/1242262502@30b075b9)
// )
df.select(concatCol).show()
// +----------------+
// |concat(str, str)|
// +----------------+
// |            hihi|
// |  welcomewelcome|
// |            toto|
// |      doricdoric|
// +----------------+
//
```

And won't allow to do any operation that is not logic in compile time.
```scala
val stringPlusInt = col[Int]("int") + col[String]("str")
// error: type mismatch;
//  found   : doric.DoricColumn[String]
//  required: doric.DoricColumn[Int]
// val stringPlusInt = col[Int]("int") + col[String]("str")
//                                                  ^
```
 This way we won't have any kind of unexpected behaviour in our process.
 
## Doric doesn't force you to use it all the time
Doric only adds method to your everyday Spark Dataframe, you can mix spark selects and doric.

```scala
import org.apache.spark.sql.{functions => f}
df
  .select(f.concat(f.col("str"), f.lit("!!!")) as "newCol") //pure spark
  .select(concat(lit("???"), colString("newCol")) as "finalCol") //pure and sweet doric
  .show()
// +-------------+
// |     finalCol|
// +-------------+
// |     ???hi!!!|
// |???welcome!!!|
// |     ???to!!!|
// |  ???doric!!!|
// +-------------+
//
```

Also, if you don't want to use doric to transform a column, you can transform a pure spark column into doric, and be sure that the type of the transformation is ok.
```scala
df.select(f.col("str").asDoric[String]).show()
// +-------+
// |    str|
// +-------+
// |     hi|
// |welcome|
// |     to|
// |  doric|
// +-------+
//
```

But we recomend to use allways the columns selectors from doric to prevent errors that doric can detect in compile time
```scala
val sparkToDoricColumn = (f.col("str") + f.lit(true)).asDoric[String]
df.select(sparkToDoricColumn).show
// doric.sem.DoricMultiError: Found 1 error in select
//   cannot resolve '(CAST(`str` AS DOUBLE) + true)' due to data type mismatch: differing types in '(CAST(`str` AS DOUBLE) + true)' (double and boolean).;
//   'Project [(cast(str#17 as double) + true) AS (str + true)#55]
//   +- Project [value#14 AS str#17]
//      +- LocalRelation [value#14]
//   
//   	located at . (intro.md:102)
// 
// 	at doric.sem.package$ErrorThrower.$anonfun$returnOrThrow$1(package.scala:9)
// 	at cats.data.Validated.fold(Validated.scala:29)
// 	at doric.sem.package$ErrorThrower.returnOrThrow(package.scala:9)
// 	at doric.sem.TransformOps$DataframeTransformationSyntax.select(TransformOps.scala:79)
// 	at repl.MdocSession$App$$anonfun$14.apply$mcV$sp(intro.md:103)
// 	at repl.MdocSession$App$$anonfun$14.apply(intro.md:101)
// 	at repl.MdocSession$App$$anonfun$14.apply(intro.md:101)
// Caused by: org.apache.spark.sql.AnalysisException: cannot resolve '(CAST(`str` AS DOUBLE) + true)' due to data type mismatch: differing types in '(CAST(`str` AS DOUBLE) + true)' (double and boolean).;
// 'Project [(cast(str#17 as double) + true) AS (str + true)#55]
// +- Project [value#14 AS str#17]
//    +- LocalRelation [value#14]
// 
// 	at org.apache.spark.sql.catalyst.analysis.package$AnalysisErrorAt.failAnalysis(package.scala:42)
// 	at org.apache.spark.sql.catalyst.analysis.CheckAnalysis$$anonfun$$nestedInanonfun$checkAnalysis$1$2.applyOrElse(CheckAnalysis.scala:161)
// 	at org.apache.spark.sql.catalyst.analysis.CheckAnalysis$$anonfun$$nestedInanonfun$checkAnalysis$1$2.applyOrElse(CheckAnalysis.scala:152)
// 	at org.apache.spark.sql.catalyst.trees.TreeNode.$anonfun$transformUp$2(TreeNode.scala:342)
// 	at org.apache.spark.sql.catalyst.trees.CurrentOrigin$.withOrigin(TreeNode.scala:74)
// 	at org.apache.spark.sql.catalyst.trees.TreeNode.transformUp(TreeNode.scala:342)
// 	at org.apache.spark.sql.catalyst.trees.TreeNode.$anonfun$transformUp$1(TreeNode.scala:339)
// 	at org.apache.spark.sql.catalyst.trees.TreeNode.$anonfun$mapChildren$1(TreeNode.scala:408)
// 	at org.apache.spark.sql.catalyst.trees.TreeNode.mapProductIterator(TreeNode.scala:244)
// 	at org.apache.spark.sql.catalyst.trees.TreeNode.mapChildren(TreeNode.scala:406)
// 	at org.apache.spark.sql.catalyst.trees.TreeNode.mapChildren(TreeNode.scala:359)
// 	at org.apache.spark.sql.catalyst.trees.TreeNode.transformUp(TreeNode.scala:339)
// 	at org.apache.spark.sql.catalyst.plans.QueryPlan.$anonfun$transformExpressionsUp$1(QueryPlan.scala:104)
// 	at org.apache.spark.sql.catalyst.plans.QueryPlan.$anonfun$mapExpressions$1(QueryPlan.scala:116)
// 	at org.apache.spark.sql.catalyst.trees.CurrentOrigin$.withOrigin(TreeNode.scala:74)
// 	at org.apache.spark.sql.catalyst.plans.QueryPlan.transformExpression$1(QueryPlan.scala:116)
// 	at org.apache.spark.sql.catalyst.plans.QueryPlan.recursiveTransform$1(QueryPlan.scala:127)
// 	at org.apache.spark.sql.catalyst.plans.QueryPlan.$anonfun$mapExpressions$3(QueryPlan.scala:132)
// 	at scala.collection.TraversableLike.$anonfun$map$1(TraversableLike.scala:286)
// 	at scala.collection.mutable.ResizableArray.foreach(ResizableArray.scala:62)
// 	at scala.collection.mutable.ResizableArray.foreach$(ResizableArray.scala:55)
// 	at scala.collection.mutable.ArrayBuffer.foreach(ArrayBuffer.scala:49)
// 	at scala.collection.TraversableLike.map(TraversableLike.scala:286)
// 	at scala.collection.TraversableLike.map$(TraversableLike.scala:279)
// 	at scala.collection.AbstractTraversable.map(Traversable.scala:108)
// 	at org.apache.spark.sql.catalyst.plans.QueryPlan.recursiveTransform$1(QueryPlan.scala:132)
// 	at org.apache.spark.sql.catalyst.plans.QueryPlan.$anonfun$mapExpressions$4(QueryPlan.scala:137)
// 	at org.apache.spark.sql.catalyst.trees.TreeNode.mapProductIterator(TreeNode.scala:244)
// 	at org.apache.spark.sql.catalyst.plans.QueryPlan.mapExpressions(QueryPlan.scala:137)
// 	at org.apache.spark.sql.catalyst.plans.QueryPlan.transformExpressionsUp(QueryPlan.scala:104)
// 	at org.apache.spark.sql.catalyst.analysis.CheckAnalysis.$anonfun$checkAnalysis$1(CheckAnalysis.scala:152)
// 	at org.apache.spark.sql.catalyst.analysis.CheckAnalysis.$anonfun$checkAnalysis$1$adapted(CheckAnalysis.scala:93)
// 	at org.apache.spark.sql.catalyst.trees.TreeNode.foreachUp(TreeNode.scala:184)
// 	at org.apache.spark.sql.catalyst.analysis.CheckAnalysis.checkAnalysis(CheckAnalysis.scala:93)
// 	at org.apache.spark.sql.catalyst.analysis.CheckAnalysis.checkAnalysis$(CheckAnalysis.scala:90)
// 	at org.apache.spark.sql.catalyst.analysis.Analyzer.checkAnalysis(Analyzer.scala:155)
// 	at org.apache.spark.sql.catalyst.analysis.Analyzer.$anonfun$executeAndCheck$1(Analyzer.scala:176)
// 	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper$.markInAnalyzer(AnalysisHelper.scala:228)
// 	at org.apache.spark.sql.catalyst.analysis.Analyzer.executeAndCheck(Analyzer.scala:173)
// 	at org.apache.spark.sql.execution.QueryExecution.$anonfun$analyzed$1(QueryExecution.scala:73)
// 	at org.apache.spark.sql.catalyst.QueryPlanningTracker.measurePhase(QueryPlanningTracker.scala:111)
// 	at org.apache.spark.sql.execution.QueryExecution.$anonfun$executePhase$1(QueryExecution.scala:143)
// 	at org.apache.spark.sql.SparkSession.withActive(SparkSession.scala:775)
// 	at org.apache.spark.sql.execution.QueryExecution.executePhase(QueryExecution.scala:143)
// 	at org.apache.spark.sql.execution.QueryExecution.analyzed$lzycompute(QueryExecution.scala:73)
// 	at org.apache.spark.sql.execution.QueryExecution.analyzed(QueryExecution.scala:71)
// 	at org.apache.spark.sql.execution.QueryExecution.assertAnalyzed(QueryExecution.scala:63)
// 	at org.apache.spark.sql.Dataset$.$anonfun$ofRows$1(Dataset.scala:90)
// 	at org.apache.spark.sql.SparkSession.withActive(SparkSession.scala:775)
// 	at org.apache.spark.sql.Dataset$.ofRows(Dataset.scala:88)
// 	at org.apache.spark.sql.Dataset.withPlan(Dataset.scala:3715)
// 	at org.apache.spark.sql.Dataset.select(Dataset.scala:1462)
// 	at doric.DoricColumn$.$anonfun$apply$1(DoricColumn.scala:29)
// 	at cats.data.Kleisli.$anonfun$map$1(Kleisli.scala:40)
// 	at cats.data.Kleisli.$anonfun$map$1(Kleisli.scala:40)
// 	at cats.data.Kleisli.$anonfun$map$1(Kleisli.scala:40)
// 	at doric.sem.TransformOps$DataframeTransformationSyntax.select(TransformOps.scala:77)
// 	... 3 more
```

In spark the sum of a string with a boolean will throw an error in runtime. In doric this code won't be able to compile.
```scala
col[String]("str") + true.lit
// error: type mismatch;
//  found   : doric.DoricColumn[Boolean]
//  required: String
// col[String]("str") + true.lit
//                      ^^^^^^^^
```

## Sweet doric syntax sugar
### Column selector alias
We know that doric can be seen as an extra boilerplate to get the columns, that's why we provide some extra methods to aquire the columns.
```scala
colString("str") // similar to col[String]("str")
// res6: DoricColumn[String] = DoricColumn(
//   Kleisli(doric.types.SparkType$$Lambda$2630/2087974216@49318c2a)
// ) // similar to col[String]("str")
colInt("int") // similar to col[Int]("int")
// res7: DoricColumn[Int] = DoricColumn(
//   Kleisli(doric.types.SparkType$$Lambda$2630/2087974216@17adbecf)
// ) // similar to col[Int]("int")
colArray[Int]("int") // similar to col[Array[Int]]("int")
// res8: DoricColumn[Array[Int]] = DoricColumn(
//   Kleisli(doric.types.SparkType$$Lambda$2630/2087974216@59eb987a)
// )
```

Doric tries to be less SQL verbose, and adopt a more object oriented API, allowing the developer to view with the dot notation of scala the methods that can be used.
```scala
val dfArrays = List(("string", Array(1,2,3))).toDF("str", "arr")
// dfArrays: DataFrame = [str: string, arr: array<int>]
```
Spark SQL method-argument way to develop
```scala
import org.apache.spark.sql.Column

val sArrCol: Column = f.col("arr")
// sArrCol: Column = arr
val sAddedOne: Column = f.transform(sArrCol, x => x + 1)
// sAddedOne: Column = transform(arr, lambdafunction((x_0 + 1), x_0))
val sAddedAll: Column = f.aggregate(sArrCol, f.lit(0), (x, y) => x + y)
// sAddedAll: Column = aggregate(arr, 0, lambdafunction((x_1 + y_2), x_1, y_2), lambdafunction(x_3, x_3))

dfArrays.select(sAddedAll as "complexTransformation").show
// +---------------------+
// |complexTransformation|
// +---------------------+
// |                    6|
// +---------------------+
//
```
This complex transformation has to ve developed in steps, and usually tested step by step. Also, the one line version is not easy to read
```scala
val complexS = f.aggregate(f.transform(f.col("arr"), x => x + 1), f.lit(0), (x, y) => x + y)
// complexS: Column = aggregate(transform(arr, lambdafunction((x_4 + 1), x_4)), 0, lambdafunction((x_5 + y_6), x_5, y_6), lambdafunction(x_7, x_7))

dfArrays.select(complexS as "complexTransformation").show
// +---------------------+
// |complexTransformation|
// +---------------------+
// |                    9|
// +---------------------+
//
```

Doric's way
```scala
val dArrCol: DoricColumn[Array[Int]] = col[Array[Int]]("arr")
// dArrCol: DoricColumn[Array[Int]] = DoricColumn(
//   Kleisli(doric.types.SparkType$$Lambda$2630/2087974216@74214ac3)
// )
val dAddedOne: DoricColumn[Array[Int]] = dArrCol.transform(x => x + 1.lit)
// dAddedOne: DoricColumn[Array[Int]] = DoricColumn(
//   Kleisli(cats.data.Kleisli$$Lambda$2639/1242262502@e00c94e)
// )
val dAddedAll: DoricColumn[Int] = dAddedOne.aggregate[Int](0.lit)((x, y) => x + y)
// dAddedAll: DoricColumn[Int] = DoricColumn(
//   Kleisli(cats.data.Kleisli$$Lambda$2639/1242262502@b2db8bc)
// )

dfArrays.select(dAddedOne as "complexTransformation").show
// +---------------------+
// |complexTransformation|
// +---------------------+
// |            [2, 3, 4]|
// +---------------------+
//
```
We know all the time what type of data we will have, so is much easier to keep track of what we can do, and simplify the line o a single:
```scala
val complexCol: DoricColumn[Int] = col[Array[Int]]("arr")
  .transform(_ + 1.lit)
  .aggregate(0.lit)(_ + _)
// complexCol: DoricColumn[Int] = DoricColumn(
//   Kleisli(cats.data.Kleisli$$Lambda$2639/1242262502@1c0ec502)
// )
  
dfArrays.select(complexCol as "complexTransformation").show
// +---------------------+
// |complexTransformation|
// +---------------------+
// |                    9|
// +---------------------+
//
```

### Literal conversions
Some times spark allows to add direct literal values to simplify code
```scala
val intDF = List(1,2,3).toDF("int")
// intDF: DataFrame = [int: int]
val colS = f.col("int") + 1
// colS: Column = (int + 1)

intDF.select(colS).show
// +---------+
// |(int + 1)|
// +---------+
// |        2|
// |        3|
// |        4|
// +---------+
//
```

Doric is a little more strict, forcing to transform this values to literal columns
```scala
val colD = colInt("int") + 1.lit
// colD: DoricColumn[Int] = DoricColumn(
//   Kleisli(cats.data.Kleisli$$Lambda$2639/1242262502@6287e312)
// )

intDF.select(colD).show
// +---------+
// |(int + 1)|
// +---------+
// |        2|
// |        3|
// |        4|
// +---------+
//
```

This is de basic flavor to work with doric, but this obvious transformations can be suggarice if we import an implicit conversion
```scala
import doric.implicitConversions.literalConversion
val colSugarD = colInt("int") + 1
// colSugarD: DoricColumn[Int] = DoricColumn(
//   Kleisli(cats.data.Kleisli$$Lambda$2639/1242262502@6a450cb5)
// )
val columConcatLiterals = concat("this", "is","doric") // concat expects DoricColumn[String] values, the conversion puts them as expected
// columConcatLiterals: DoricColumn[String] = DoricColumn(
//   Kleisli(cats.data.Kleisli$$Lambda$2639/1242262502@c4bb119)
// ) // concat expects DoricColumn[String] values, the conversion puts them as expected

intDF.select(colSugarD, columConcatLiterals).show
// +---------+-----------------------+
// |(int + 1)|concat(this, is, doric)|
// +---------+-----------------------+
// |        2|            thisisdoric|
// |        3|            thisisdoric|
// |        4|            thisisdoric|
// +---------+-----------------------+
//
```

This conversion will transform any pure scala value, to it's representation in a doric column, only if the type is valid
```scala
colInt("int") + 1f //an integer with a float value cant be directly added in doric
// error: type mismatch;
//  found   : Float(1.0)
//  required: doric.DoricColumn[Int]
// colInt("int") + 1f //an integer with a float value cant be directly added in doric
//                 ^^
```
```scala
concat("hi", 5) // expects only strings and a integer is found
// error: type mismatch;
//  found   : Int(5)
//  required: doric.DoricColumn[String]
// concat("hi", 5) // expects only strings and a integer is found
//              ^
```
