lazy val `play-doc` = (project in file("."))
  .enablePlugins(PlayLibrary, SbtTwirl)

libraryDependencies ++= Seq(
  "org.pegdown" % "pegdown" % "1.4.0",
  "commons-io" % "commons-io" % "2.4",
  "org.specs2" %% "specs2" % "2.3.11" % "test"
)

// Important to use 1.6 target, since ASM doesn't support 1.8 byte code
javacOptions in compile := javacOptions.value ++ Seq("-source", "1.6", "-target", "1.6")

playBuildRepoName in ThisBuild := "play-doc"
