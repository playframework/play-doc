lazy val `play-doc` = (project in file("."))
  .enablePlugins(PlayLibrary, SbtTwirl, PlayReleaseBase)

// we need crossVersionScala since interplay won't build PlayLibrary for 2.10.6
crossScalaVersions := Seq("2.10.6", "2.11.8")

libraryDependencies ++= Seq(
  "org.pegdown" % "pegdown" % "1.6.0",
  "commons-io" % "commons-io" % "2.5",
  "org.specs2" %% "specs2-core" % "3.8.4" % "test"
)

// ASM version 5 will support Java 8 but not Java 9
javacOptions in compile := javacOptions.value ++ Seq("-source", "1.8", "-target", "1.8")
javacOptions in doc := javacOptions.value ++ Seq("-source", "1.8")

playBuildRepoName in ThisBuild := "play-doc"
