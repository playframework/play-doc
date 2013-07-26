package play.doc

import org.specs2.mutable.Specification
import java.io.File

object PlayDocSpec extends Specification {

  def fileFromClasspath(name: String) = new File(Thread.currentThread.getContextClassLoader.getResource(name).toURI)
  val repo = new FilesystemRepository(fileFromClasspath("file-placeholder").getParentFile)
  val renderer = new PlayDoc(repo, repo, "resources", "2.1.3")

  "code snippet handling" should {
    def test(label: String, rendered: String) = {
      renderer.render("@[" + label + "](code/sample.txt)") must_==
        """<pre><code class="txt">""" + rendered + """</code></pre>"""
    }

    "allow extracting code snippets" in test("simple", "Snippet")
    "strip indent of shallowest line" in test("indent", "  deep\nshallow")
    "ignore other characters on label delimiter line" in test("otherchars", "Snippet")
    "allow skipping lines" in test("skipping", "Snippet")
    "allow skipping multiple lines" in test("skipmultiple", "Snippet")
    "allow replacing lines" in test("replacing", "Replaced\nSnippet")
    "allow replacing lines with limited text" in test("replacelimited", "Replaced")
    "allow inserting lines" in test("inserting", "Inserted\nSnippet")
    "allow inserting lines with limited text" in test("insertlimited", "Inserted")
  }

  "page rendering" should {
    "render the page and the sidebar" in {
      val result = renderer.renderPage("Foo")
      result must beSome.like {
        case (main, maybeSidebar) => {
          main must contain("Some markdown")
          main must not contain("Sidebar")
          maybeSidebar must beSome.like {
            case sidebar => {
              sidebar must contain("Sidebar")
              sidebar must not contain("Some markdown")
            }
          }
        }
      }

    }
  }

  "play version variables" should {
    "be substituted with the Play version" in {
      renderer.render("The current Play version is %PLAY_VERSION%") must_== "<p>The current Play version is 2.1.3</p>"
    }
    "work in verbatim blocks" in {
      renderer.render(
        """
          | Here is some code:
          |
          |     addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "%PLAY_VERSION%")
          |
        """.stripMargin) must contain("% &quot;2.1.3&quot;)")
    }
  }

}
