---
title: Doric Documentation
permalink: docs/
---


# Quick start

## Installation

To use doric, just add the following dependency in your favourite build tool:

#### Sbt
```scala
libraryDependencies += "org.hablapps" % "doric_2.12" % "@STABLE_VERSION@"
```
#### Maven
```xml
<dependency>
  <groupId>org.hablapps</groupId>
  <artifactId>doric_2.12</artifactId>
  <version>@STABLE_VERSION@</version>
</dependency>
```

Doric is committed to use the most modern APIs first.
* Doric is compatible with Spark version @SPARK_VERSION@.
* The latest stable version of doric is @STABLE_VERSION@.
* The latest experimental version of doric is @VERSION@.

## Type-safe column expressions

Doric is very easy to work with. First, you require the following import clause:

```scala mdoc
import doric._
```

There is no problem in combining conventional Spark column expressions and doric columns. 
However, to avoid name clashes, we will use the prefix `f` for the former ones:

```scala mdoc
import org.apache.spark.sql.{functions => f}
``` 

Next, make sure to activate the flag `spark.sql.datetime.java8API.enabled` when creating the Spark context.

```scala mdoc:invisible
val spark = doric.DocInit.getSpark
import spark.implicits._
```

The overall purpose of doric is providing a type-safe API on top of the DataFrame API. This essentially means 
that we aim at capturing errors at compile time. For instance, in Spark we can't mix apples and oranges, but this 
code still compiles:
```scala mdoc
def df = List(1,2,3).toDF.select($"value" * f.lit(true))
```
It's only when we try to construct the DataFrame that an exception is raised at _run-time_:
```scala mdoc:crash
df
``` 

Using doric, there is no need to wait for so long: errors will be reported at compile-time!
```scala mdoc:fail
// should be removed
import doric.implicitConversions._

List(1,2,3).toDF.select(col[Int]("value") * lit(true))
```

As you may see, changes in column expressions are minimal: just annotate column references with the intended type, 
i.e. `col[Int]("name")`, instead of `col("name")`. With this extra bit of type information, we are not only
referring to a column named `name`: we are signalling that the expected Spark data type of that column is `Integer`. 
In order to refer to columns of other data types, doric strictly follows the 
[mapping](https://spark.apache.org/docs/latest/sql-ref-datatypes.html) defined by Spark SQL itself. 

---
**NOTE**

Of course, this only works if you, the programmer, know the intended type 
of the column at compile-time. In a pure dynamic setting, doric is useless. Note, however, that you don't need to know 
in advance the whole row type as with Datasets. Thus, doric sits between a wholehearted static setting and a 
purely dynamic one. It offers type-safety for column expressions at a minimum cost, without compromising performance, 
i.e. sticking to DataFrames.

---

Finally, once we have constructed a doric column expression, we can use it within the context of a `withColumn` expression, 
or, in general, wherever we may use plain Spark columns: joins, filters, etc.:

```scala mdoc
List(1,2,3).toDF.filter(col[Int](c"value") > lit(1))
```

As you can see in [validations](intro.md), the explicit type annotations enable further validations when columns
are interpreted within the context of `withColumn`, `select`, etc.

## Mixing doric and Spark columns

Since doric is intended as a replacement of the _whole_ DataFrame API, type-safe versions of Spark functions 
for numbers, dates, strings, etc., are provided. To know all possible transformations, you can take a look at 
the [DoricColumn API](https://www.hablapps.com/doric/docs/api/latest/doric/DoricColumn.html) .Occasionally, however, we might need to mix 
both doric and Spark column expressions. There is no problem with that, as this example shows: 

```scala mdoc
val strDf = List("hi", "welcome", "to", "doric").toDF("str")

strDf
  .select(f.concat(f.col("str"), f.lit("!!!")) as "newCol") //pure spark
  .select(concat(lit("???"), colString(c"newCol")) as c"finalCol") //pure and sweet doric
  .show()
```

Also, we can transform pure Spark columns into doric columns, and be sure that specific doric [validations](validations.md)
will be applied:
```scala mdoc
strDf.select(f.col("str").asDoric[String]).show()
```

```scala mdoc:crash

strDf.select((f.col("str") + f.lit(true)).asDoric[String]).show
```

