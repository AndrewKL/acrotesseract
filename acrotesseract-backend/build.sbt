ThisBuild / organization := "info.acrotesseract"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.9.0"
// Emit Java 21 bytecode (the Lambda runtime) whatever JDK runs sbt.
ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature", "-Wunused:all", "-release:21")
ThisBuild / javacOptions ++= Seq("--release", "21")

val jsoniterVersion = "2.41.0"

lazy val commonTest = Seq(
  libraryDependencies += "org.scalameta" %% "munit" % "1.3.6" % Test
)

// Pure domain model: Pose, Transition. No AWS dependencies.
lazy val domain = (project in file("modules/domain"))
  .settings(commonTest)

// HTTP-agnostic API layer: routing, handlers, JSON codecs.
lazy val api = (project in file("modules/api"))
  .dependsOn(domain)
  .settings(
    commonTest,
    libraryDependencies ++= Seq(
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % jsoniterVersion,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % "compile-internal"
    )
  )

// AWS Lambda entrypoint (API Gateway HTTP API, payload v2). Assembled into the JAR that CDK deploys.
lazy val lambda = (project in file("modules/lambda"))
  .dependsOn(api)
  .settings(
    commonTest,
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.4.0",
      "com.amazonaws" % "aws-lambda-java-events" % "3.16.1"
    ),
    assembly / assemblyJarName := "acrotesseract-lambda.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "versions", _*)   => MergeStrategy.first
      case PathList("META-INF", "MANIFEST.MF")     => MergeStrategy.discard
      case PathList("META-INF", xs @ _*) if xs.lastOption.exists(_.endsWith(".SF")) => MergeStrategy.discard
      case "module-info.class"                     => MergeStrategy.discard
      case x =>
        val old = (assembly / assemblyMergeStrategy).value
        old(x)
    }
  )

// Local HTTP server around the api module, for `nx serve acrotesseract-backend`.
lazy val local = (project in file("modules/local"))
  .dependsOn(api)
  .settings(
    Compile / run / fork := true,
    Compile / run / envVars := Map("STAGE" -> "local")
  )

lazy val root = (project in file("."))
  .aggregate(domain, api, lambda, local)
  .settings(publish / skip := true)
