import sbt.{Compile, Def}

val spark24Version = "2.4.8"
val spark30Version = "3.0.3"
val spark31Version = "3.1.3"
val spark32Version = "3.2.1"

val sparkShort: String => String = {
  case "2.4" => spark24Version
  case "3.0" => spark30Version
  case "3.1" => spark31Version
  case "3.2" => spark32Version
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
    "AlfonsoRR",
    "Eduardo Ruiz",
    "",
    url("https://github.com/eruizalo")
  )
)

Global / scalaVersion := "2.12.15"

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

val sparkVersion = settingKey[String]("Spark version")

val configSpark = Seq(
  sparkVersion := sparkShort(System.getProperty("sparkVersion", "3.1"))
)

lazy val core = project
  .in(file("core"))
  .settings(
    configSpark,
    name       := "doric",
    run / fork := true,
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql"     % sparkVersion.value % "provided",
      "org.typelevel"    %% "cats-core"     % "2.7.0",
      "com.lihaoyi"      %% "sourcecode"    % "0.2.8",
      "io.monix"         %% "newtypes-core" % "0.2.1",
      "com.github.mrpowers" %% "spark-daria"      % "1.2.3"  % "test",
      "com.github.mrpowers" %% "spark-fast-tests" % "1.2.0"  % "test",
      "org.scalatest"       %% "scalatest"        % "3.2.11" % "test"
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
        case `spark24Version` =>
          Seq(
            (Compile / sourceDirectory)(_ / "spark_2.4_mount" / "scala")
          ).join.value
        case `spark30Version` =>
          Seq(
            (Compile / sourceDirectory)(_ / "spark_3.0" / "scala"),
            (Compile / sourceDirectory)(_ / "spark_3.0_mount" / "scala")
          ).join.value
        case `spark31Version` =>
          Seq(
            (Compile / sourceDirectory)(_ / "spark_3.0" / "scala"),
            (Compile / sourceDirectory)(_ / "spark_3.1" / "scala"),
            (Compile / sourceDirectory)(_ / "spark_3.1_mount" / "scala")
          ).join.value
        case `spark32Version` =>
          Seq(
            (Compile / sourceDirectory)(_ / "spark_3.0" / "scala"),
            (Compile / sourceDirectory)(_ / "spark_3.1" / "scala"),
            (Compile / sourceDirectory)(_ / "spark_3.2" / "scala"),
            (Compile / sourceDirectory)(_ / "spark_3.2_mount" / "scala")
          ).join.value
      }
    },
    Test / unmanagedSourceDirectories ++= {
      sparkVersion.value match {
        case `spark24Version` =>
          Seq.empty[Def.Initialize[File]].join.value
        case `spark30Version` =>
          Seq(
            (Test / sourceDirectory)(_ / "spark_3.0" / "scala")
          ).join.value
        case `spark31Version` =>
          Seq(
            (Test / sourceDirectory)(_ / "spark_3.0" / "scala"),
            (Test / sourceDirectory)(_ / "spark_3.1" / "scala")
          ).join.value
        case `spark32Version` =>
          Seq(
            (Test / sourceDirectory)(_ / "spark_3.0" / "scala"),
            (Test / sourceDirectory)(_ / "spark_3.1" / "scala"),
            (Test / sourceDirectory)(_ / "spark_3.2" / "scala")
          ).join.value
      }
    }
  )

lazy val docs = project
  .in(file("docs"))
  .dependsOn(core)
  .settings(
    configSpark,
    run / fork := true,
    run / javaOptions += "-XX:MaxJavaStackTraceDepth=10",
    mdocIn := baseDirectory.value / "docs",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % sparkVersion.value
    ),
    mdocVariables := Map(
      "VERSION"        -> version.value,
      "STABLE_VERSION" -> "0.0.2",
      "SPARK_VERSION"  -> spark31Version
    ),
    mdocExtraArguments := Seq(
      "--clean-target"
    )
  )
  .enablePlugins(MdocPlugin)

Global / scalacOptions ++= Seq(
  "-encoding",
  "utf8",             // Option and arguments on same line
  "-Xfatal-warnings", // New lines for each options
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps",
  "-Ypartial-unification",
  "-Ywarn-numeric-widen"
)

// Scoverage settings
Global / coverageEnabled       := false
Global / coverageFailOnMinimum := false
Global / coverageHighlighting  := true
