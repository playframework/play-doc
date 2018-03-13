# play-doc

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
