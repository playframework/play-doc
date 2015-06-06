lazy val `play-doc` = (project in file("."))
  .enablePlugins(SbtTwirl, PlayLibrary, PlayReleaseBase)

libraryDependencies ++= Seq(
  "org.pegdown" % "pegdown" % "1.5.0",
  "commons-io" % "commons-io" % "2.4",
  "org.specs2" %% "specs2" % "2.3.11" % "test"
)

javacOptions in Compile := Seq("-source", "1.6", "-target", "1.6")

playBuildRepoName in ThisBuild := "play-doc"