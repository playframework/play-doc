// Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>

resolvers += Resolver.sonatypeCentralSnapshots

addSbtPlugin("org.playframework.twirl" % "sbt-twirl"      % "2.1.0-M9+126-adca2222-SNAPSHOT")
addSbtPlugin("org.scalameta"           % "sbt-scalafmt"   % "2.6.2")
addSbtPlugin("com.github.sbt"          % "sbt-ci-release" % "1.12.1")
addSbtPlugin("com.github.sbt"          % "sbt-header"     % "5.11.0")
