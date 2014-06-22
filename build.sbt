organization := "com.typesafe.play"

name := "play-doc"

version := "1.1.0"

crossScalaVersions := Seq("2.10.4", "2.11.1")

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "org.pegdown" % "pegdown" % "1.4.0",
  "commons-io" % "commons-io" % "2.4",
  "org.specs2" %% "specs2" % "2.3.11" % "test"
)

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

publishTo <<= version { version =>
  if (version.endsWith("SNAPSHOT"))
    Some("Typesafe Maven Snapshots Repository" at "https://private-repo.typesafe.com/typesafe/maven-snapshots/")
  else
    Some("Typesafe Maven Releases Repository" at "https://private-repo.typesafe.com/typesafe/maven-releases/")
}
