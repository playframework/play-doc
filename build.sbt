import interplay.ScalaVersions
import interplay.ScalaVersions._

lazy val `play-doc` = (project in file("."))
  .enablePlugins(PlayLibrary, SbtTwirl, PlayReleaseBase)

// We need crossVersionScala since interplay won't build PlayLibrary for 2.10.
crossScalaVersions := Seq(ScalaVersions.scala210, scala211, scala212, "2.13.0-M5")

libraryDependencies ++= Seq(
  "org.pegdown" % "pegdown" % "1.6.0",
  "commons-io" % "commons-io" % "2.6"
) ++ specs2Deps(scalaVersion.value)

def specs2Deps(scalaVer: String): Seq[ModuleID] = scalaVer match {
  case ScalaVersions.scala210 => Seq("org.specs2" %% "specs2-core" % "3.9.5" % Test)
  case _ => Seq("org.specs2" %% "specs2-core" % "4.3.5" % Test)
}

// ASM version 5 will support Java 8 but not Java 9
javacOptions in compile := javacOptions.value ++ Seq("-source", "1.8", "-target", "1.8")
javacOptions in doc := javacOptions.value ++ Seq("-source", "1.8")

playBuildRepoName in ThisBuild := "play-doc"
