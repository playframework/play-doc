# play-doc

[![Twitter Follow](https://img.shields.io/twitter/follow/playframework?label=follow&style=flat&logo=twitter&color=brightgreen)](https://twitter.com/playframework)
[![Discord](https://img.shields.io/discord/931647755942776882?logo=discord&logoColor=white)](https://discord.gg/g5s2vtZ4Fa)
[![GitHub Discussions](https://img.shields.io/github/discussions/playframework/playframework?&logo=github&color=brightgreen)](https://github.com/playframework/playframework/discussions)
[![StackOverflow](https://img.shields.io/static/v1?label=stackoverflow&logo=stackoverflow&logoColor=fe7a16&color=brightgreen&message=playframework)](https://stackoverflow.com/tags/playframework)
[![YouTube](https://img.shields.io/youtube/channel/views/UCRp6QDm5SDjbIuisUpxV9cg?label=watch&logo=youtube&style=flat&color=brightgreen&logoColor=ff0000)](https://www.youtube.com/channel/UCRp6QDm5SDjbIuisUpxV9cg)
[![Twitch Status](https://img.shields.io/twitch/status/playframework?logo=twitch&logoColor=white&color=brightgreen&label=live%20stream)](https://www.twitch.tv/playframework)
[![OpenCollective](https://img.shields.io/opencollective/all/playframework?label=financial%20contributors&logo=open-collective)](https://opencollective.com/playframework)

[![Build Status](https://github.com/playframework/play-doc/actions/workflows/build-test.yml/badge.svg)](https://github.com/playframework/play-doc/actions/workflows/build-test.yml)
[![Maven](https://img.shields.io/maven-central/v/com.typesafe.play/play-doc_2.13.svg?logo=apache-maven)](https://mvnrepository.com/artifact/com.typesafe.play/play-doc_2.13)
[![Repository size](https://img.shields.io/github/repo-size/playframework/play-doc.svg?logo=git)](https://github.com/playframework/play-doc)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)
[![Mergify Status](https://img.shields.io/endpoint.svg?url=https://api.mergify.com/v1/badges/playframework/play-doc&style=flat)](https://mergify.com)

This project implements Plays documentation parsing and generating.  Documentation is in markdown with an extension that allows code snippets to come from external files.  It is used by Play to allow us to have all our documentation code snippets in external compiled and tested files, ensuring that they are accurate and up to date.

## Markdown Syntax

Referencing code in an external file looks like this:

```markdown
@[label](some/relative/path)
```

## Code snippet syntax

Code snippets are identified using the hash symbol prepended to the label, like this:

```scala
//#label
println("Hello world")
//#label
```

The label can be anywhere in a line, and the code snippet will be every line between the start and end labels, exclusive.  Typically the snippet might be part of a test, for example:

```scala
"List" should {
    "support fold" in {
    //#fold
    val list = List(1, 2, 3)
    val sum = list.fold { (a, b) => a + b }
    //#fold
    sum must_== 6
    }
}
```

In some rare cases, you may need to slightly modify the snippet as it stands.  Typically this might be when naming conflicts exist in the code, for example, if you want to show an entire Java class with its package name, and you have multiple places in your docs where you want to do similar, you might do this:

```scala
//#label
//#replace package foo.bar;
package foo.bar.docs.sec1;

class Foo {
    ...
}
//#label
```

The supported directives are replace, skip (followed by the number of lines to skip) and insert.

## Play Version

Any %PLAY_VERSION% variables will be substituted with the current version of Play.
