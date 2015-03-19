organization := "com.typesafe.play"

name := "play-doc"

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

releaseSettings

crossScalaVersions := Seq("2.10.4", "2.11.1")

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "org.pegdown" % "pegdown" % "1.4.0",
  "commons-io" % "commons-io" % "2.4",
  "org.specs2" %% "specs2" % "2.3.11" % "test"
)

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

// Publishing
publishTo := {
  if (isSnapshot.value) Some(Opts.resolver.sonatypeSnapshots)
  else Some(Opts.resolver.sonatypeStaging)
}

ReleaseKeys.crossBuild := true

ReleaseKeys.tagName := (version in ThisBuild).value

// When https://github.com/sbt/sbt-release/pull/49 is fixed, this can be updated to not be so hacky
publish := PgpKeys.publishSigned.value

homepage := Some(url("https://github.com/playframework/play-doc"))

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

pomExtra := {
  <scm>
    <url>https://github.com/playframework/play-doc</url>
    <connection>scm:git:git@github.com:playframework/play-doc.git</connection>
  </scm>
  <developers>
    <developer>
      <id>jroper</id>
      <name>James Roper</name>
      <url>https://jazzy.id.au</url>
    </developer>
    <developer>
      <id>richdougherty</id>
      <name>Rich Dougherty</name>
      <url>http://www.richdougherty.com</url>
    </developer>
    <developer>
      <id>wsargent</id>
      <name>Will Sargent</name>
      <url>https://github.com/wsargent</url>
    </developer>
  </developers>
}

pomIncludeRepository := { _ => false }
