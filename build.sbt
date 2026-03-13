import scala.language.postfixOps

//import sbt.Compile

val stableVersion = "0.0.8"

// Spark 4.x only (dropped support for Spark 2.x and 3.x)
val sparkDefaultVersion = "4.0.1"
val scalaVersion213     = "2.13.16"

val versionRegex = """^(.*)\.(.*)\.(.*)$""".r

// Parse Spark version from system property or use default
val sparkVersion = settingKey[String]("Spark version")
Global / sparkVersion := sys.props.getOrElse("sparkVersion", sparkDefaultVersion)

// Scala version selection based on Spark version
val scalaVersionSelect: String => String = {
  case versionRegex("4", _, _) => scalaVersion213  // Spark 4.x uses Scala 2.13
  case v => throw new IllegalArgumentException(
    s"Unsupported Spark version: $v. Only Spark 4.x is supported."
  )
}

val long2ShortVersion: String => String = { case versionRegex(a, b, _) =>
  s"$a.$b"
}

ThisBuild / organization := "org.hablapps"
ThisBuild / homepage     := Some(url("https://github.com/hablapps/doric"))
ThisBuild / licenses := List(
  "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")
)
ThisBuild / developers := List(
  Developer(
    "AlfonsoRR",
    "Alfonso Roa",
    "@saco_pepe",
    url("https://github.com/alfonsorr")
  ),
  Developer(
    "eruizalo",
    "Eduardo Ruiz",
    "",
    url("https://github.com/eruizalo")
  )
)
Global / scalaVersion    := scalaVersionSelect(sparkVersion.value)
Global / publish / skip  := true
Global / publishArtifact := false

// scaladoc settings
Compile / doc / scalacOptions ++= Seq("-groups")

// test suite settings
Test / fork := true
javaOptions ++= Seq(
  "-Xms512M",
  "-Xmx2048M",
  "-XX:MaxPermSize=2048M",
  "-XX:+CMSClassUnloadingEnabled"
)
// Show runtime of tests
Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD")

scmInfo := Some(
  ScmInfo(
    url("https://github.com/hablapps/doric"),
    "git@github.com:hablapps/doric.git"
  )
)

updateOptions := updateOptions.value.withLatestSnapshots(false)

val scalaOptionsCommon = Seq(
  "-encoding",
  "utf8",             // Option and arguments on same line
  "-Xfatal-warnings", // New lines for each options
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps",
  "-Ywarn-numeric-widen"
)
lazy val core = project
  .in(file("core"))
  .settings(
    name               := "doric_" + long2ShortVersion(sparkVersion.value),
    run / fork         := true,
    publish / skip     := false,
    publishArtifact    := true,
    scalaVersion       := scalaVersionSelect(sparkVersion.value),
    crossScalaVersions := Seq(scalaVersionSelect(sparkVersion.value)),
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % sparkVersion.value % "provided",
      "org.typelevel"          %% "cats-core"               % "2.13.0",
      "com.lihaoyi"            %% "sourcecode"              % "0.4.2",
      "com.chuusai"            %% "shapeless"               % "2.3.13",
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.13.0",
      "com.github.mrpowers"    %% "spark-fast-tests"        % "1.3.0"  % "test",
      "org.scalatest"          %% "scalatest"               % "3.2.19" % "test"
    ),
    // Fix for IllegalAccessException with ZoneInfo in Spark 4.0 + Java 17+
    Test / fork := true,
    Test / javaOptions ++= Seq(
      "--add-opens=java.base/sun.util.calendar=ALL-UNNAMED"
    ),
    // docs
    run / fork                      := true,
    Compile / doc / autoAPIMappings := true,
    Compile / doc / scalacOptions ++= Seq(
      "-groups",
      "-implicits",
      "-skip-packages",
      "org.apache.spark"
    ),
    Compile / unmanagedSourceDirectories ++= {
      sparkVersion.value match {
        case versionRegex(major, minor, _) =>
          Seq((Compile / sourceDirectory).value / s"spark_$major.$minor" / "scala")
      }
    },
    Test / unmanagedSourceDirectories ++= {
      sparkVersion.value match {
        case versionRegex(major, minor, _) =>
          Seq((Test / sourceDirectory).value / s"spark_$major.$minor" / "scala")
      }
    },
    scalacOptions ++= scalaOptionsCommon
  )

// Spark 4.x uses mdoc
val plugins = List(MdocPlugin)

lazy val docs = project
  .in(file("docs"))
  .dependsOn(core)
  .settings(
    run / fork      := true,
    publish / skip  := true,
    publishArtifact := false,
    run / javaOptions += "-XX:MaxJavaStackTraceDepth=10",
    scalaVersion := scalaVersionSelect(sparkVersion.value),
    mdocIn       := baseDirectory.value / "docs",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % sparkVersion.value
    ),
    mdocVariables := Map(
      "VERSION"        -> version.value,
      "STABLE_VERSION" -> stableVersion,
      "SPARK_VERSION"  -> sparkVersion.value,
      "SPARK_SHORT_VERSION" -> long2ShortVersion(sparkVersion.value)
        .replace(".", "-"),
      "SCALA_SHORT_VERSION" -> long2ShortVersion(scalaVersion.value)
    ),
    mdocExtraArguments := Seq(
      "--clean-target"
    ),
    scalacOptions ++= scalaOptionsCommon
  )
  .enablePlugins(plugins *)

// Scoverage settings
Global / coverageEnabled       := false
Global / coverageFailOnMinimum := false
Global / coverageHighlighting  := true
