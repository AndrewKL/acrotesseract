name := "AcroTesseract"
 
version := "1.0" 
      
lazy val `acrotesseract` = (project in file(".")).enablePlugins(PlayScala)

enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)


resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
scalaVersion := "2.11.11"

libraryDependencies ++= Seq( 
  jdbc , 
  cache , 
  ws , 
  specs2 % Test,
  "junit" % "junit" % "4.11" % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test
    exclude("junit", "junit-dep")
)

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
      