import interplay.ScalaVersions._

// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
(ThisBuild / dynverVTagPrefix) := false

// Sanity-check: assert that version comes from a tag (e.g. not a too-shallow clone)
// https://github.com/dwijnand/sbt-dynver/#sanity-checking-the-version
Global / onLoad := (Global / onLoad).value.andThen { s =>
  dynverAssertTagVersion.value
  s
}

lazy val `play-doc` = (project in file("."))
  .enablePlugins(PlayLibrary, SbtTwirl)
  .settings(
    crossScalaVersions := Seq(scala212, scala213, scala3),
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) =>
          Seq(
            "-Xlint",
            "-Ywarn-unused:imports",
            "-Xlint:nullary-unit",
            "-Ywarn-dead-code",
            "-Xsource:3",
          )
        case _ => Nil
      }
    }
  )

libraryDependencies ++= Seq(
  ("org.pegdown"  % "pegdown"        % "1.6.0").exclude("org.parboiled", "parboiled-java"),
  "org.parboiled" % "parboiled-java" % "1.4.1",
  "commons-io"    % "commons-io"     % "2.12.0",
  "org.specs2"   %% "specs2-core"    % "4.20.0" % Test
)

javacOptions ++= Seq(
  "--release",
  "11",
  "-Xlint:deprecation",
  "-Xlint:unchecked",
)

scalacOptions ++= Seq(
  "-release",
  "11",
)

(ThisBuild / playBuildRepoName) := "play-doc"

addCommandAlias(
  "validateCode",
  List(
    "scalafmtSbtCheck",
    "scalafmtCheckAll",
  ).mkString(";")
)
