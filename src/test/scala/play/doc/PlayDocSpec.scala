package play.doc

import org.specs2.mutable._
import java.io.File

class PlayDocSpec extends Specification {
  def fileFromClasspath(name: String) = new File(Thread.currentThread.getContextClassLoader.getResource(name).toURI)
  val repo                            = new FilesystemRepository(fileFromClasspath("file-placeholder").getParentFile)
  val oldRenderer = new PlayDoc(repo, repo, "resources", "2.1.3", None, new TranslatedPlayDocTemplates("Next"), None)

  val renderer =
    new PlayDoc(
      repo,
      repo,
      "resources",
      "2.4.0",
      PageIndex.parseFrom(repo, "Home", Some("example")),
      new TranslatedPlayDocTemplates("Next"),
      None
    )

  "code snippet handling" should {
    def test(label: String, rendered: String, file: String = "code/sample.txt") = {
      oldRenderer.render(s"@[$label]($file)") must_==
        s"""<pre class="prettyprint"><code class="language-txt">$rendered</code></pre>"""
    }

    def failTest(label: String) = {
      oldRenderer.render("@[" + label + "](code/sample.txt)") must_==
        """Unable to find label """ + label + """ in source file """ + "code/sample.txt"
    }

    "allow extracting code snippets" in test("simple", "Snippet")

    "allow extracting code snippets using string that exists as substring elsewhere" in test("one", "One")
    "allow extracting code snippets using string as full string" in test(
      "onetwothree",
      "One Two Three"
    )                                                                               // paired with previous test
    "fail on substring code snippets using string as trailing" in failTest("three") // paired with previous test

    "fail on substring with no full string match" in failTest("leading")
    "should match on full string" in test("leading-following", "Leading Following") // paired with test for exception
    "fail on substring when looking for a trailing match" in failTest("following")

    "strip indent of shallowest line" in test("indent", "  deep\nshallow")
    "ignore other characters on label delimiter line" in test("otherchars", "Snippet")
    "allow skipping lines" in test("skipping", "Snippet")
    "allow skipping multiple lines" in test("skipmultiple", "Snippet")
    "allow replacing lines" in test("replacing", "Replaced\nSnippet")
    "allow replacing lines with limited text" in test("replacelimited", "Replaced")
    "allow inserting lines" in test("inserting", "Inserted\nSnippet")
    "allow inserting lines with limited text" in test("insertlimited", "Inserted")

    "allow including a whole file" in test("", "This is the whole file", "code/whole.txt")
  }

  "page rendering" should {
    "old renderer" in {
      "render the page and the sidebar" in {
        val result = oldRenderer.renderPage("Foo")
        result must beSome[RenderedPage].like { case RenderedPage(main, maybeSidebar, path, maybeBreadcrumbs) =>
          main must contain("Some markdown")
          main must not contain "Sidebar"
          maybeSidebar must beSome[String].like { case sidebar =>
            sidebar must contain("Sidebar")
            sidebar must not contain "Some markdown"
          }
          maybeBreadcrumbs must beSome[String].like { case breadcrumbs =>
            breadcrumbs must contain("Breadcrumbs")
            breadcrumbs must not contain "Some markdown"
          }
          path must_== "example/docs/Foo.md"
        }
      }
    }

    "new renderer" in {
      "render the page and sidebar" in {
        val result = renderer.renderPage("Foo")
        result must beSome[RenderedPage].like { case RenderedPage(main, maybeSidebar, path, maybeBreadcrumbs) =>
          main must contain("Some markdown")
          maybeSidebar must beSome[String].like { case sidebar =>
            sidebar must contain("<a href=\"Home\">Documentation Home</a>")
          }
          maybeBreadcrumbs must beSome[String].like { case breadcrumbs =>
            breadcrumbs must contain(
              "<a itemprop=\"item\" href=\"Home\"><span itemprop=\"name\" title=\"Home\">Home</span></a>"
            )
          }
          path must_== "example/docs/Foo.md"
        }
      }
    }
  }

  "play version variables" should {
    "be substituted with the Play version" in {
      oldRenderer.render(
        "The current Play version is %PLAY_VERSION%"
      ) must_== "<p>The current Play version is 2.1.3</p>"
    }
    "work in verbatim blocks" in {
      oldRenderer.render("""
                           | Here is some code:
                           |
                           |     addSbtPlugin("org.playframework" % "sbt-plugin" % "%PLAY_VERSION%")
                           |
        """.stripMargin) must contain("% &quot;2.1.3&quot;)")
    }
    "work in code blocks" in {
      oldRenderer.render("Play `%PLAY_VERSION%` is cool.") must_== "<p>Play <code>2.1.3</code> is cool.</p>"
    }
  }

  "play table of contents support" should {
    "render a table of contents" in {
      renderer.renderPage("Home") must beSome[RenderedPage].like { case page =>
        page.html must contain("<h2>Sub Documentation</h2>")
        page.html must contain("<a href=\"Foo\">Foo Page</a>")
        page.html must contain("<a href=\"SubFoo1\">Sub Section</a>")
        page.html must contain("<a href=\"SubFoo1\">Sub Foo Page 1</a>")
      }
    }
  }

  "header link rendering" should {
    "render header links" in {
      renderer.render("# Hello World") must_==
        """<h1 id="Hello-World"><a class="section-marker" href="#Hello-World">§</a>Hello World</h1>"""
    }
  }
}
