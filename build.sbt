name := "AcroTesseract"
 
version := "1.0" 
      
lazy val `acrotesseract` = (project in file(".")).enablePlugins(PlayScala)

enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)


resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
scalaVersion := "2.11.11"

libraryDependencies ++= Seq( 
  jdbc ,
  ws ,
  guice,
  "com.amazonaws" % "aws-java-sdk" % "1.11.421",
  "mysql" % "mysql-connector-java" % "8.0.12",
  "com.typesafe.play" %% "anorm" % "2.5.3",
  "com.gu" %% "play-googleauth" % "0.7.7",
  "com.iterable" %% "iterableplayutils" % "2.0.0",

  "org.flywaydb" % "flyway-core" % "5.2.0" % Test,
  specs2 % Test,
  "junit" % "junit" % "4.11" % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test
    exclude("junit", "junit-dep")
)

dependencyOverrides += "io.netty" % "netty-codec-http" % "4.0.41.Final"
dependencyOverrides += "io.netty" % "netty-handler" % "4.0.41.Final"
dependencyOverrides += "io.netty" % "netty-codec" % "4.0.41.Final"
dependencyOverrides += "io.netty" % "netty-transport" % "4.0.41.Final"
dependencyOverrides += "io.netty" % "netty-buffer" % "4.0.41.Final"
dependencyOverrides += "io.netty" % "netty-common" % "4.0.41.Final"
dependencyOverrides += "io.netty" % "netty-transport-native-epoll" % "4.0.41.Final"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

dockerfile in docker := {
  val appDir: File = stage.value
  val targetDir = "/app"

  new Dockerfile {
    from("openjdk:8-jre")
    entryPoint(s"$targetDir/bin/${executableScriptName.value}")
    copy(appDir, targetDir, chown = "daemon:daemon")
  }
}
      