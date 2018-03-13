lazy val `play-doc` = (project in file("."))
  .enablePlugins(PlayLibrary, SbtTwirl, PlayReleaseBase)

// We need crossVersionScala since interplay won't build PlayLibrary for 2.10.
crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.4", "2.13.0-M3")

libraryDependencies ++= Seq(
  "org.pegdown" % "pegdown" % "1.6.0",
  "commons-io" % "commons-io" % "2.6"
) ++ specs2Deps(scalaVersion.value)

def specs2Deps(scalaVer: String) = CrossVersion.partialVersion(scalaVer) match {
  case Some((2, v)) if v >= 12 => Seq("org.specs2" %% "specs2-core" % "4.0.3" % Test)
  case _ => Seq("org.specs2" %% "specs2-core" % "3.8.6" % Test)
}

// ASM version 5 will support Java 8 but not Java 9
javacOptions in compile := javacOptions.value ++ Seq("-source", "1.8", "-target", "1.8")
javacOptions in doc := javacOptions.value ++ Seq("-source", "1.8")

playBuildRepoName in ThisBuild := "play-doc"
