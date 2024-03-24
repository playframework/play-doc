// Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>

import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.HeaderPattern.commentBetween
import de.heikoseeberger.sbtheader.CommentStyle
import de.heikoseeberger.sbtheader.FileType
import de.heikoseeberger.sbtheader.LineCommentCreator
// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
(ThisBuild / dynverVTagPrefix) := false

// Sanity-check: assert that version comes from a tag (e.g. not a too-shallow clone)
// https://github.com/dwijnand/sbt-dynver/#sanity-checking-the-version
Global / onLoad := (Global / onLoad).value.andThen { s =>
  dynverAssertTagVersion.value
  s
}

lazy val `play-doc` = (project in file("."))
  .enablePlugins(Omnidoc, SbtTwirl, HeaderPlugin)
  .settings(
    organization         := "org.playframework",
    organizationName     := "The Play Framework Project",
    organizationHomepage := Some(url("https://playframework.com")),
    homepage             := Some(url(s"https://github.com/playframework/${Omnidoc.repoName}")),
    licenses             := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),
    crossScalaVersions   := Seq("2.12.19", "2.13.13", "3.3.3"),
    developers += Developer(
      "playframework",
      "The Play Framework Contributors",
      "contact@playframework.com",
      url("https://github.com/playframework")
    ),
    headerLicense := Some(
      HeaderLicense.Custom(
        "Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>"
      )
    ),
    headerMappings ++= Map(
      FileType("sbt")        -> HeaderCommentStyle.cppStyleLineComment,
      FileType("properties") -> HeaderCommentStyle.hashLineComment,
      FileType("md") -> CommentStyle(new LineCommentCreator("<!---", "-->"), commentBetween("<!---", "*", "-->"))
    ),
    (Compile / headerSources) ++=
      ((baseDirectory.value ** ("*.properties" || "*.md" || "*.sbt"))
        --- (baseDirectory.value ** "target" ** "*")
        --- (baseDirectory.value / "src/test/resources" ** "*")).get ++
        (baseDirectory.value / "project" ** "*.scala" --- (baseDirectory.value ** "target" ** "*")).get,
    (Test / headerResources) := Seq(),
    pomIncludeRepository     := { _ => false },
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) =>
          Seq(
            "-Xlint",
            "-Ywarn-unused:imports",
            "-Xlint:nullary-unit",
            "-Ywarn-dead-code",
            "-Xsource:3",
            "-Xmigration",
          )
        case _ => Nil
      }
    }
  )

libraryDependencies ++= Seq(
  ("org.pegdown"  % "pegdown"        % "1.6.0").exclude("org.parboiled", "parboiled-java"),
  "org.parboiled" % "parboiled-java" % "1.4.1",
  "commons-io"    % "commons-io"     % "2.15.1",
  "org.specs2"   %% "specs2-core"    % "4.20.5" % Test
)

javacOptions ++= Seq(
  "--release",
  "11",
  "-Xlint:deprecation",
  "-Xlint:unchecked",
  "-Xlint:-options",
  "-encoding",
  "UTF-8",
)
doc / javacOptions := Seq("-source", "11")

scalacOptions ++= Seq(
  "-release",
  "11",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-encoding",
  "utf8"
)

addCommandAlias(
  "validateCode",
  List(
    "headerCheckAll",
    "scalafmtSbtCheck",
    "scalafmtCheckAll",
  ).mkString(";")
)
