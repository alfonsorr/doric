---
title: Custom types in doric permalink: docs/custom/
---
# Custom types
Bored that spark limits the interaction with the outside world? Need a more descriptive value to insert into your
dataframe? Doric's got your back with his custom types. The only thing you need to know is the datatype of spark that
will represent your value, this is easier to see with an example.

## User as a string
Lets think a field with the name and the surname of a
user, we want to represent it outside of spark as a case class:

```scala
case class User(name: String, surname: String)
```

But we don't want to represent it in spark as a struct, it will be better to represent it as a simple string. In doric
this is super simple, just type this code:

```scala
import doric.types.SparkType

implicit val userSparkType: SparkType[User] = SparkType[String].customType[User](
        x => s"${x.name}#${x.surname}",
        x => {
          val name :: surname :: Nil = x.split("#").toList
          User(name, surname)
        }
      )
// userSparkType: SparkType[User] = doric.types.SparkType$$anon$1@1a139347
```

Lets take a closer look, first we are creating a implicit `SparkType` for `User`. And the way to do this is invoquing
the implicit SparkType of the original SparkType we want to base our custom type, calling `SparkType[String]`. One we
have it, we can call the method `customType`. This method needs to lambdas, the first one will transform from our custom
type to the base type, in our case we create a single string with the character `#` as a separator, the second one is
the opossite function, will transform from `String` to our custom `User`, in our case, split the String by the
character `#`  and reconstruct the `User` class.

Now we have a valid SparkType, we can use it for everything:
* use it as a literal
```scala
df.withColumn("user", User("John", "Doe").lit)
// res1: DataFrame = [user: string]
```
* select a column with type `User`
```scala
import doric.implicitConversions.literalConversion
df.withColumn("expectedUser", col[User]("user") === User("John", "Doe"))
// res2: DataFrame = [user: string, expectedUser: boolean]
```
* collect the column and obtain a `User` in your driver
```scala
println(df.collectCols(col[User]("user")))
// List(User(Jane,Doe))
```

We have to allways keep in mind that inside our dataframe, the user is represented as a String:
```scala
df.select(User("John", "Doe").lit.as("user")).printSchema
// root
//  |-- user: string (nullable = false)
//
```
So can be a good idea to create a casting to the string value, in this case spark wont do nothing because its a `String` inside.
```scala
import doric.types.SparkCasting
implicit val userStringCast = SparkCasting[User, String]
// userStringCast: types.Casting[User, String] = doric.types.SparkCasting$$anon$1@21c34bf8
```

But the real power of this custom types is the ability to create also custom functions for the `DoricColumn[User]`
```scala
implicit class DoricUserMethods(u: DoricColumn[User]) {
  def name: StringColumn = u.cast[String].split("#").getIndex(0)
  def surname: StringColumn = u.cast[String].split("#").getIndex(1)
}

df.filter(col[User]("user").name === "John")
// res5: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [user: string]
```
The power for a much reusable and descriptive code at your service.

## Other example
A simple case, give context to a numeric value that represent the state of a person. In our data we have a column that
represents if the user is single(1), in a relation(2) or married(3), but inside the dataframe is the number. We would
first create our classes:

```scala
sealed trait UserState
object Single extends UserState
object Relation extends UserState
object Married extends UserState
```

Now we can create our `SparkType[UserState]` using `Int` as our base `DataType`

```scala
val stateFromSpark: Int => UserState = {
  case 1 => Single
  case 2 => Relation
  case 3 => Married
  case _ => throw new Exception("not a valid value")
}
// stateFromSpark: Int => UserState = <function1>

val stateToSpark: UserState => Int = {
  case Single => 1
  case Relation => 2
  case Married => 3
}
// stateToSpark: UserState => Int = <function1>

implicit val userStateSparkType: SparkType[UserState] = SparkType[Int].customType(stateToSpark, stateFromSpark)
// userStateSparkType: SparkType[UserState] = doric.types.SparkType$$anon$1@559e3f67
```

## Custom types with type parameters
Doric not only allows to create simple types, it can create complex types like `Set`, represented in spark as an array. We will need to know that our type inside of spark will still be an array, with the main difference that if we insert something in our column it can be repeated.
This is as simple as create the following lines:
```
import scala.reflect.api.TypeTag
implicit def setSparkType[T: SparkType: TypeTag]: SparkType[Set[T]] =
  SparkType[Array[T]].customType(_.toSet, _.toArray)
```
