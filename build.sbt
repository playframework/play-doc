import interplay.ScalaVersions
import interplay.ScalaVersions._

// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
dynverVTagPrefix in ThisBuild := false

// Sanity-check: assert that version comes from a tag (e.g. not a too-shallow clone)
// https://github.com/dwijnand/sbt-dynver/#sanity-checking-the-version
Global / onLoad := (Global / onLoad).value.andThen { s =>
  val v = version.value
  if (dynverGitDescribeOutput.value.hasNoTags)
    throw new MessageOnlyException(
      s"Failed to derive version from git tags. Maybe run `git fetch --unshallow`? Version: $v"
    )
  s
}

lazy val `play-doc` = (project in file("."))
  .enablePlugins(PlayLibrary, SbtTwirl, PlayReleaseBase)
  .settings(
    Seq(
      releaseProcess := {
        import ReleaseTransformations._
        Seq[ReleaseStep](
          checkSnapshotDependencies,
          runClean,
          releaseStepCommandAndRemaining("+test"),
          releaseStepCommandAndRemaining("+publishSigned"),
          releaseStepCommand("sonatypeBundleRelease"),
          pushChanges // <- this needs to be removed when releasing from tag
        )
      }
    )
  )

// We need crossVersionScala since interplay won't build PlayLibrary for 2.10.
crossScalaVersions := Seq(scala210, scala212, scala213)

libraryDependencies ++= Seq(
  "org.pegdown" % "pegdown"    % "1.6.0",
  "commons-io"  % "commons-io" % "2.6"
) ++ specs2Deps(scalaVersion.value)

def specs2Deps(scalaVer: String): Seq[ModuleID] = scalaVer match {
  case ScalaVersions.scala210 => Seq("org.specs2" %% "specs2-core" % "3.9.5" % Test)
  case _                      => Seq("org.specs2" %% "specs2-core" % "4.8.1" % Test)
}

javacOptions ++= Seq(
  "-source",
  "1.8",
  "-target",
  "1.8",
  "-Xlint:deprecation",
  "-Xlint:unchecked",
)

scalacOptions ++= {
  if (scalaVersion.value.equals(scala210)) {
    Seq("-target:jvm-1.8")
  } else {
    Seq(
      "-target:jvm-1.8",
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
  }
}

playBuildRepoName in ThisBuild := "play-doc"
