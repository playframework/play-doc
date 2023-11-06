/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.doc

import java.io.File

import org.specs2.mutable.Specification

class PageIndexSpec extends Specification {
  def fileFromClasspath(name: String) = new File(Thread.currentThread.getContextClassLoader.getResource(name).toURI)
  val repo                            = new FilesystemRepository(fileFromClasspath("file-placeholder").getParentFile)
  def maybeIndex                      = PageIndex.parseFrom(repo, "Home", Some("example"))
  def index                           = maybeIndex.getOrElse(new PageIndex(Toc("", "", Nil)))

  "Page Index " should {
    "parse the index" in {
      maybeIndex must beSome(anInstanceOf[PageIndex])
    }

    "provide access to pages" in {
      "top level" in {
        index.get("Home") must beSome[Page].like { case page =>
          page.page must_== "Home"
          page.path must beSome("example")
          page.title must_== "Documentation Home"
        }
      }
      "first level" in {
        index.get("Foo") must beSome[Page].like { case page =>
          page.page must_== "Foo"
          page.path must beSome("example/docs")
          page.title must_== "Foo Page"
        }
      }
      "deep" in {
        index.get("SubFoo1") must beSome[Page].like { case page =>
          page.page must_== "SubFoo1"
          page.path must beSome("example/docs/sub")
          page.title must_== "Sub Foo Page 1"
        }
        index.get("SubFoo2") must beSome[Page].like { case page =>
          page.page must_== "SubFoo2"
          page.path must beSome("example/docs/sub")
          page.title must_== "Sub Foo Page 2"
        }
      }
      "non existent" in {
        index.get("NotExists") must beNone
      }
    }

    "provide a table of contents" in {
      index.toc.nodes.collectFirst { case ("Home", n) => n } must beSome(TocPage("Home", "Documentation Home", None))
      index.toc.nodes.collectFirst { case ("docs", n) => n } must beSome[TocTree].like { case toc: Toc =>
        toc.title must_== "Sub Documentation"
        toc.nodes.collectFirst { case ("Foo", n) => n } must beSome(TocPage("Foo", "Foo Page", Some(List("SubFoo2"))))
        toc.nodes.collectFirst { case ("sub", n) => n } must beSome[TocTree].like { case toc: Toc =>
          toc.title must_== "Sub Section"
          toc.nodes.collectFirst { case ("SubFoo1", n) => n } must beSome(
            TocPage("SubFoo1", "Sub Foo Page 1", None)
          )
          toc.nodes.collectFirst { case ("SubFoo2", n) => n } must beSome(
            TocPage("SubFoo2", "Sub Foo Page 2", None)
          )
        }
      }
    }

    "provide navigation" in {
      index.get("SubFoo1") must beSome[Page].like { case page =>
        page.nav.size must_== 3
        page.nav(0).title must_== "Sub Section"
        page.nav(1).title must_== "Sub Documentation"
        page.nav(2).title must_== "Home"
      }
    }

    "provide the next page" in {
      index.get("Home").flatMap(_.next) must beSome[TocTree].like { case toc: Toc =>
        toc.name must_== "docs"
      }
      index.get("Foo").flatMap(_.next) must beSome[TocTree].like { case toc: TocPage =>
        toc.page must_== "SubFoo2"
      }
      index.get("SubFoo1").flatMap(_.next) must beSome[TocTree].like { case toc: TocPage =>
        toc.page must_== "SubFoo2"
      }
      index.get("SubFoo2").flatMap(_.next) must beNone
    }
  }
}
