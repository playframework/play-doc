package play.doc

import org.apache.commons.io.IOUtils

/**
 * A table of contents node
 */
sealed trait TocTree {
  /**
   * The page that this node should point to
   */
  def page: String

  /**
   * The title of this node
   */
  def title: String
}

/**
 * A table of contents
 * 
 * @param title The title of this table of contents
 * @param nodes The nodes in the table of contents
 * @param descend Whether a table of contents should descend into this table of contents
 */
case class Toc(name: String, title: String, nodes: List[(String, TocTree)], descend: Boolean = true) extends TocTree {
  require(nodes.nonEmpty)
  
  def page = nodes.head._2.page  
}

/**
 * A page (leaf node) pointed to by the table of contents
 * 
 * @param page The page
 * @param title The title of the page
 */
case class TocPage(page: String, title: String) extends TocTree

/**
 * The page index
 * 
 * @param toc The table of contents
 */
class PageIndex(val toc: Toc, path: Option[String] = None) {
  
  private def indexPages(path: Option[String], nav: List[Toc], toc: Toc): List[Page] = {
    toc.nodes.flatMap {
      case (_, TocPage(page, title)) => List(Page(page, path.getOrElse(""), title, nav))
      case (pathPart, tocPart: Toc) => indexPages(
        path.map(_ + "/" + pathPart).orElse(Some(pathPart)), tocPart :: nav, tocPart
      )
    }
  }
  
  private val byPage: Map[String, Page] = {
    val allPages = indexPages(path, List(toc), toc)
    allPages.map(p => p.page -> p).toMap
  }

  /**
   * Get the page for the given page name
   */
  def get(page: String): Option[Page] = byPage.get(page)
  
}

/**
 * A page
 *
 * @param page The page name
 * @param path The path to the page
 * @param title The title of the page
 * @param nav The navigation associated with the page, this is a list of all the table of contents nodes, starting from
 *            the one that this page is in, all the way up the tree to the root node
 */
case class Page(page: String, path: String, title: String, nav: List[Toc]) {
  def fullPath = path + "/" + page

  lazy val next: Option[TocTree] = findNext(page, nav)

  private def findNext(name: String, nav: List[Toc]): Option[TocTree] = {
    nav match {
      case Nil => None
      case toc :: rest =>
        toc.nodes.view
          .dropWhile(_._1 != name)
          .drop(1)
          .headOption
          .map(_._2)
          .orElse(
            findNext(toc.name, rest)
          )
    }
  }
}

object PageIndex {

  def parseFrom(repo: FileRepository, path: Option[String] = None): Option[PageIndex] = {
    parseToc(repo, path, "", "Home") match {
      case toc: Toc => Some(new PageIndex(toc, path))
      case _ => None
    }
  }

  private def parseToc(repo: FileRepository, path: Option[String], page: String, title: String,
                       descend: Boolean = true): TocTree = {
    repo.loadFile(path.fold("index.toc")(_ + "/index.toc"))(IOUtils.toString).fold[TocTree](
      TocPage(page, title)
    ) { content =>
      val lines = content.lines.toList.map(_.trim).filter(_.nonEmpty)
      // Remaining lines are the entries of the contents
      val tocNodes = lines.map { entry =>
        val (link, title) = {
          entry.split(":", 2) match {
            case Array(p) => p -> p
            case Array(p, t) => p -> t
          }
        }
        val (relPath, descend) = if (link.startsWith("!")) {
          link.drop(1) -> false
        } else {
          link -> true
        }
        relPath -> parseToc(repo, path.map(_ + "/" + relPath).orElse(Some(relPath)), relPath, title, descend)

      }
      Toc(page, title, tocNodes, descend)
    }
  }

}