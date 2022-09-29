// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
(ThisBuild / dynverVTagPrefix) := false

// Sanity-check: assert that version comes from a tag (e.g. not a too-shallow clone)
// https://github.com/dwijnand/sbt-dynver/#sanity-checking-the-version
Global / onLoad := (Global / onLoad).value.andThen { s =>
  dynverAssertTagVersion.value
  s
}

lazy val `play-doc` = (project in file("."))
  .enablePlugins(PlayLibrary, SbtTwirl, PlayReleaseBase)

libraryDependencies ++= Seq(
  "org.pegdown" % "pegdown"     % "1.6.0",
  "commons-io"  % "commons-io"  % "2.11.0",
  "org.specs2" %% "specs2-core" % "4.16.1" % Test
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
  "-encoding",
  "utf8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Ywarn-unused:imports",
  "-Xlint:nullary-unit",
  "-Ywarn-dead-code",
)

(ThisBuild / playBuildRepoName) := "play-doc"

addCommandAlias(
  "validateCode",
  List(
    "scalafmtSbtCheck",
    "scalafmtCheckAll",
  ).mkString(";")
)
