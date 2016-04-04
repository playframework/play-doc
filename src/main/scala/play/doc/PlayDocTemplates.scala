package play.doc

/**
  * Templates for rendering Play documentation snippets.
  */
trait PlayDocTemplates {

  /**
    * Render the next link.
    *
    * @param toc The table of contents.
    * @return The next link.
    */
  def nextLink(toc: TocTree): String

  /**
    * Render the sidebar.
    *
    * @param hierarchy The hierarchy to render in the sidebar.
    * @return The sidebar.
    */
  def sidebar(hierarchy: List[Toc]): String

  /**
   *
   * @param hierarchy The hierarchy to render in the breadcrumbs.
   * @return
   */
  def breadcrumbs(hierarchy: List[Toc]): String

  /**
    * Render a table of contents.
    *
    * @param toc The table of contents to render.
    * @return The table of contents.
    */
  def toc(toc: Toc): String
}

class TranslatedPlayDocTemplates(nextText: String) extends PlayDocTemplates {
  override def nextLink(toc: TocTree): String = play.doc.html.nextLink(toc, nextText).body
  override def sidebar(heirarchy: List[Toc]): String = play.doc.html.sidebar(heirarchy).body
  override def breadcrumbs(hierarchy: List[Toc]): String = play.doc.html.breadcrumbs(hierarchy).body
  override def toc(toc: Toc): String = play.doc.html.toc(toc).body
}

object PlayDocTemplates extends TranslatedPlayDocTemplates("Next")
