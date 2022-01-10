---
title: Doric Documentation
permalink: docs/modularity/
---

```scala mdoc:invisible
import org.apache.spark.sql.{functions => f}

val spark = doric.DocInit.getSpark
import spark.implicits._

import doric._
import doric.implicitConversions._
```

# Fostering modularity

Doric main focus is to give the possibility of modularize the logic for the etl, and the idea to find fast the location of an error is essential.
Let's make an example, imagine that we have some columns have a suffix in the name telling us is information of a user.

```scala mdoc:invisible
val userDF = List(
("Foo", "Madrid", 35),
("Bar", "New York", 40),
("John", "Paris", 30)
).toDF("name_user", "city_user", "age_user")
```

```scala mdoc
userDF.show
userDF.printSchema
```

Us as developers want to abstract from this suffix and focus only in the unique part of the name:
```scala mdoc
colString(c"name_user")
colInt(c"age_user")
colString(c"city_user")
//and many more
```
So we can make a function to simplify it:
```scala mdoc
import doric.types.SparkType
def user[T: SparkType](colName: CName): DoricColumn[T] = {
  col[T](colName + "_user")
}
```
In valid cases it works ok, bug when an error is produce because one of these references, it will point to the line `col[T](colName + "_user")` that is not the real problem.

```scala mdoc:crash
val userc = user[Int](c"name") //wrong type :S
userDF.select(userc)
```

What we really want is to mark as the source the place we are using our `user` method. We can achieve this by adding only an implicit value to the definition:
```scala mdoc:invisible:reset
val spark = doric.DocInit.getSpark
      
import spark.implicits._

val userDF = List(
("Foo", "Madrid", 35),
("Bar", "New York", 40),
("John", "Paris", 30)
).toDF("name_user", "city_user", "age_user")

```

```scala mdoc
import doric._
import doric.sem.Location
import doric.types.SparkType

def user[T: SparkType](colName: CName)(implicit location: Location): DoricColumn[T] = {
  col[T](colName + "_user")
}
```
Now if we repeat the same error we will be pointed to the real problem
```scala mdoc:crash
val age = user[Int](c"name")
val team = user[String](c"team")
userDF.select(age, team)
```

