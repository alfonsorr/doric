---
title: Pingpointing the error place
permalink: docs/errors
---

# Errors in doric
Doric is a typesafe APO, this means that the compiler will prevent to do transformations that will throw exceptions in runtime.
The only possible source of errors are the selection of columns, if the column doesn't exist or if the column contains an unexpected type.

Let's see an example of an error
```scala
import doric._

val df = List(("hi", 31)).toDF("str", "int")
// df: DataFrame = [str: string, int: int]
val col1 = colInt("str")
// col1: DoricColumn[Int] = DoricColumn(
//   Kleisli(doric.types.SparkType$$Lambda$2631/926970862@18989e0)
// )
val col2 = colString("int")
// col2: DoricColumn[String] = DoricColumn(
//   Kleisli(doric.types.SparkType$$Lambda$2631/926970862@5ad6f98e)
// )
val col3 = colInt("unknown")
// col3: DoricColumn[Int] = DoricColumn(
//   Kleisli(doric.types.SparkType$$Lambda$2631/926970862@586728e8)
// )
```
```scala
df.select(col1, col2, col3)
// doric.sem.DoricMultiError: Found 3 errors in select
//   The column with name 'str' is of type StringType and it was expected to be IntegerType
//   	located at . (error-location.md:31)
//   The column with name 'int' is of type IntegerType and it was expected to be StringType
//   	located at . (error-location.md:34)
//   Cannot resolve column name "unknown" among (str, int)
//   	located at . (error-location.md:37)
// 
// 	at doric.sem.package$ErrorThrower.$anonfun$returnOrThrow$1(package.scala:9)
// 	at cats.data.Validated.fold(Validated.scala:29)
// 	at doric.sem.package$ErrorThrower.returnOrThrow(package.scala:9)
// 	at doric.sem.TransformOps$DataframeTransformationSyntax.select(TransformOps.scala:75)
// 	at repl.MdocSession$App$$anonfun$6.apply(error-location.md:44)
// 	at repl.MdocSession$App$$anonfun$6.apply(error-location.md:44)
```

