/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

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
 * @param title
 *   The title of this table of contents
 * @param nodes
 *   The nodes in the table of contents
 * @param descend
 *   Whether a table of contents should descend into this table of contents
 */
case class Toc(name: String, title: String, nodes: List[(String, TocTree)], descend: Boolean = true) extends TocTree {
  require(nodes.nonEmpty)

  def page = nodes.head._2.page
}

/**
 * A page (leaf node) pointed to by the table of contents
 *
 * @param page
 *   The page
 * @param title
 *   The title of the page
 * @param next
 *   Explicitly provided next links. If None, then the index structure are used to generate the next links, otherwise
 *   these links should be used. Note that `Some(Nil)` is distinct from `None`, in that `Some(Nil)` means there should
 *   be no next links, whereas `None` means let the index decide what the next link should be.
 */
case class TocPage(page: String, title: String, next: Option[List[String]]) extends TocTree

/**
 * The page index
 *
 * @param toc
 *   The table of contents
 */
class PageIndex(val toc: Toc, path: Option[String] = None) {
  private val byPage: Map[String, Page] = {
    // First, create a by name index
    def indexByName(node: TocTree): List[(String, TocTree)] =
      node match {
        case Toc(name, _, nodes, _) =>
          (name -> node) :: nodes.map(_._2).flatMap(indexByName)
        case TocPage(name, _, _) =>
          List(name -> node)
      }
    val byNameMap = indexByName(toc).toMap

    def indexPages(path: Option[String], nav: List[Toc], toc: Toc): List[Page] = {
      toc.nodes.flatMap {
        case (_, TocPage(page, title, explicitNext)) =>
          val nextLinks = explicitNext
            .map { links => links.collect(Function.unlift(byNameMap.get)) }
            .getOrElse {
              findNext(page, nav).toList
            }
          List(Page(page, path, title, nav, nextLinks))
        case (pathPart, tocPart: Toc) =>
          indexPages(
            path.map(_ + "/" + pathPart).orElse(Some(pathPart)),
            tocPart :: nav,
            tocPart
          )
      }
    }

    indexPages(path, List(toc), toc).map(p => p.page -> p).toMap
  }

  private def findNext(name: String, nav: List[Toc]): Option[TocTree] = {
    nav match {
      case Nil         => None
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

  /**
   * Get the page for the given page name
   */
  def get(page: String): Option[Page] = byPage.get(page)
}

/**
 * A page
 *
 * @param page
 *   The page name
 * @param path
 *   The path to the page
 * @param title
 *   The title of the page
 * @param nav
 *   The navigation associated with the page, this is a list of all the table of contents nodes, starting from the one
 *   that this page is in, all the way up the tree to the root node
 * @param nextLinks
 *   A list of next links
 */
case class Page(page: String, path: Option[String], title: String, nav: List[Toc], nextLinks: List[TocTree]) {
  def fullPath = path.fold(page)(_ + "/" + page)

  def next: Option[TocTree] = nextLinks.headOption
}

object PageIndex {
  def parseFrom(repo: FileRepository, home: String, path: Option[String] = None): Option[PageIndex] = {
    parseToc(repo, path, "", home) match {
      case toc: Toc => Some(new PageIndex(toc, path))
      case _        => None
    }
  }

  private def parseToc(
      repo: FileRepository,
      path: Option[String],
      page: String,
      title: String,
      descend: Boolean = true,
      next: Option[List[String]] = None
  ): TocTree = {
    repo
      .loadFile(path.fold("index.toc")(_ + "/index.toc"))(IOUtils.toString(_, "utf-8"))
      .fold[TocTree](
        TocPage(page, title, next)
      ) { content =>
        // https://github.com/scala/bug/issues/11125#issuecomment-423375868
        val lines = augmentString(content).linesIterator.toList.map(_.trim).filter(_.nonEmpty)
        // Remaining lines are the entries of the contents
        val tocNodes = lines.map { entry =>
          val linkAndTitle :: params = entry.split(";").toList
          val (link, title)          = {
            linkAndTitle.split(":", 2) match {
              case Array(p)    => p -> p
              case Array(p, t) => p -> t
            }
          }
          val parsedParams = params.map { param =>
            param.split("=", 2) match {
              case Array(k)    => k -> k
              case Array(k, v) => k -> v
            }
          }.toMap

          val next = parsedParams.get("next").map { n => n.split(",").toList }

          val (relPath, descend) = if (link.startsWith("!")) {
            link.drop(1) -> false
          } else {
            link -> true
          }

          relPath -> parseToc(repo, path.map(_ + "/" + relPath).orElse(Some(relPath)), relPath, title, descend, next)
        }
        Toc(page, title, tocNodes, descend)
      }
  }
}
