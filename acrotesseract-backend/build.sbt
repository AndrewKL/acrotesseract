ThisBuild / organization := "info.acrotesseract"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.9.0"
// Emit Java 21 bytecode (the Lambda runtime) whatever JDK runs sbt.
ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature", "-Wunused:all", "-release:21")
ThisBuild / javacOptions ++= Seq("--release", "21")

val jsoniterVersion = "2.41.0"
val awsSdkVersion = "2.55.4"

// Only the URLConnection HTTP client is used; leaving out Netty and Apache keeps the Lambda JAR small.
ThisBuild / excludeDependencies ++= Seq(
  ExclusionRule("software.amazon.awssdk", "netty-nio-client"),
  ExclusionRule("software.amazon.awssdk", "apache-client")
)

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
    // Bundle the repo's data/data.json (the static pose and transition data) onto the classpath as data.json.
    Compile / resourceGenerators += Def.task {
      val src = (LocalRootProject / baseDirectory).value.getParentFile / "data" / "data.json"
      val dest = (Compile / resourceManaged).value / "data.json"
      IO.copyFile(src, dest)
      Seq(dest)
    }.taskValue,
    libraryDependencies ++= Seq(
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % jsoniterVersion,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % "compile-internal"
    )
  )

// DynamoDB repository (AWS SDK v2) and the env-based wiring shared by Lambda and the local server.
lazy val store = (project in file("modules/store"))
  .dependsOn(api % "compile->compile;test->test")
  .settings(
    commonTest,
    libraryDependencies ++= Seq(
      "software.amazon.awssdk" % "dynamodb" % awsSdkVersion,
      "software.amazon.awssdk" % "url-connection-client" % awsSdkVersion
    ),
    // DynamoDB Local tests share tables, so run suites one at a time.
    Test / parallelExecution := false
  )

// AWS Lambda entrypoint (API Gateway HTTP API, payload v2). Assembled into the JAR that CDK deploys.
lazy val lambda = (project in file("modules/lambda"))
  .dependsOn(store)
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

val passThroughEnv = Set(
  "POSES_TABLE", "TRANSITIONS_TABLE", "DYNAMODB_ENDPOINT", "WRITES_ENABLED",
  "AWS_REGION", "AWS_PROFILE", "AWS_ACCESS_KEY_ID", "AWS_SECRET_ACCESS_KEY"
)

// Developer tools: the local HTTP server (`nx serve`) and the DynamoDB seed (`nx seed`).
// The SSO modules let the AWS SDK use `aws sso login` profiles; they stay out of the Lambda JAR.
lazy val local = (project in file("modules/local"))
  .dependsOn(store)
  .settings(
    libraryDependencies ++= Seq(
      "software.amazon.awssdk" % "sso" % awsSdkVersion,
      "software.amazon.awssdk" % "ssooidc" % awsSdkVersion
    ),
    Compile / run / fork := true,
    // `sbt local/run` starts the server; the seed runs with `local/runMain acrotesseract.seed`.
    Compile / run / mainClass := Some("acrotesseract.runLocalServer"),
    // Pass through the table settings Nx sets (see project.json `serve -c dynamodb` and `seed`).
    Compile / run / envVars := Map("STAGE" -> "local") ++ sys.env.filterKeys(passThroughEnv).toMap
  )

lazy val root = (project in file("."))
  .aggregate(domain, api, store, lambda, local)
  .settings(publish / skip := true)
