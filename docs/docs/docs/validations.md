---
title: Doric Documentation
permalink: docs/validations/
---

```scala mdoc:invisible
import org.apache.spark.sql.{functions => f}

val spark = doric.DocInit.getSpark
import spark.implicits._

import doric._
import doric.implicitConversions._
```

# Doric validations

Doric is a type-safe API, which means that many common errors will be captured at compile-time. However, there
are errors which can't be anticipated, since they depend on the actual datasource available at runtime.
For instance, we might make reference to a non-existing column. In that case, doric behaves similarly to Spark,
raising a run-time exception: 

```scala mdoc:crash
// Spark
List(1,2,3).toDF.select(f.col("id")+1)
```

```scala mdoc:crash
// Doric
List(1,2,3).toDF.select(colInt("id")+1)
```

However, there is a slight difference in the exception reported: doric adds precise information about the location
of the error in the source code, which in many cases is immensely useful (e.g. to support the development of [reusable
functions](modularity.md)). 

## Mismatch types

But doric goes further, thanks to the type annotations that it supports. Indeed, let's assume that the column 
exists but its type is not what we expected: Spark won't be able to detect that, since type expectations are not 
encoded in plain columns. Thus, the following code will compile and execute without errors:

```scala mdoc
val df = List("1","2","three").toDF.select(f.col("value") + 1)
```

and we will be able to run the DataFrame:

```scala mdoc
df.show
```

obtaining null values and garbage results, in general.

Using doric we can prevent the creation of the DataFrame, since column expressions are typed:

```scala mdoc:crash
val df = List("1","2","three").toDF.select(colInt("value") + 1.lit)
```

## Error aggregation

Doric departs from Spark in an additional aspect of error management: Sparks adopts a fail-fast strategy, in such 
a way that it will stop at the first error encountered, whereas doric will keep accumulating errors throughout the
whole column expression. This is essential to speed up and facilitate the solution to most common development problems.

For instance, let's consider the following code where we encounter three erroneous column references:

```scala mdoc:silent
val dfPair = List(("hi", 31)).toDF("str", "int")
val col1 = colInt("str")   // existing column, wrong type
val col2 = colString("int") // existing column, wrong type
val col3 = colInt("unknown") // non-existing column
```

```scala mdoc:crash
dfPair.select(col1, col2, col3)
```

As we can see, the select expression throws a _single_ exception reporting the three different errors. There is no
need to start an annoying fix-rerun loop until all errors are found. Moreover, note that each error points to the 
line of the corresponding column expression where it took place. 

