// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
(ThisBuild / dynverVTagPrefix) := false

// Sanity-check: assert that version comes from a tag (e.g. not a too-shallow clone)
// https://github.com/dwijnand/sbt-dynver/#sanity-checking-the-version
Global / onLoad := (Global / onLoad).value.andThen { s =>
  dynverAssertTagVersion.value
  s
}

lazy val `play-doc` = (project in file("."))
  .enablePlugins(Omnidoc, SbtTwirl)
  .settings(
    organization         := "com.typesafe.play",
    organizationName     := "The Play Framework Project",
    organizationHomepage := Some(url("https://playframework.com")),
    homepage             := Some(url(s"https://github.com/playframework/${Omnidoc.repoName}")),
    licenses             := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),
    crossScalaVersions   := Seq("2.12.18", "2.13.12", "3.3.1"),
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
  "commons-io"    % "commons-io"     % "2.13.0",
  "org.specs2"   %% "specs2-core"    % "4.20.2" % Test
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

addCommandAlias(
  "validateCode",
  List(
    "scalafmtSbtCheck",
    "scalafmtCheckAll",
  ).mkString(";")
)
