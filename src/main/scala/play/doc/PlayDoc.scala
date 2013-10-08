package play.doc

import java.io.File
import java.util.{Collections, Arrays}
import org.pegdown._
import org.pegdown.plugins.{ToHtmlSerializerPlugin, PegDownPlugins}
import org.pegdown.ast._
import org.apache.commons.io.IOUtils
import scala.collection.JavaConverters._
import scala.Some

/**
 * A rendered page
 *
 * @param html The HTML for the page
 * @param sidebarHtml The HTML for the sidebar
 * @param path The path that the page was found at
 */
case class RenderedPage(html: String, sidebarHtml: Option[String], path: String)

/**
 * Play documentation support
 *
 * @param markdownRepository Repository for finding markdown files
 * @param codeRepository Repository for finding code samples
 * @param resources The resources path
 * @param playVersion The version of Play we are rendering docs for.
 */
class PlayDoc(markdownRepository: FileRepository, codeRepository: FileRepository, resources: String,
              playVersion: String) {

  val PlayVersionVariableName = "%PLAY_VERSION%"

  /**
   * Render some markdown.
   */
  def render(markdown: String, relativePath: Option[File] = None): String = {
    withRenderer(relativePath)(_(markdown))
  }

  /**
   * Render a Play documentation page.
   *
   * @param page The page to render, without path or markdown extension.
   * @return If found a tuple of the rendered page and the rendered sidebar, if the sidebar was found.
   */
  def renderPage(page: String): Option[RenderedPage] = {

    // Find the markdown file
    markdownRepository.findFileWithName(page + ".md").flatMap { pagePath =>

      val file = new File(pagePath)
      // Work out the relative path for the file
      val relativePath = Option(file.getParentFile)

      withRenderer(relativePath) { renderer =>

        def render(path: String): Option[String] = {
          markdownRepository.loadFile(path)(IOUtils.toString).map(renderer)
        }

        // Recursively search for Sidebar
        def findSideBar(file: Option[File]): Option[String] = file match {
          case None => None
          case Some(parent) => {
            val sidebar = render(parent.getPath + "/_Sidebar.md")
            sidebar.orElse(findSideBar(Option(parent.getParentFile)))
          }
        }

        // Render both the markdown and the sidebar
        render(pagePath).map { markdown =>
          RenderedPage(markdown, findSideBar(relativePath), pagePath)
        }
      }
    }
  }

  private def withRenderer[T](relativePath: Option[File])(block: (String => String) => T): T = {

    // Link renderer
    val link: (String => (String, String)) = _ match {
      case link if link.contains("|") => {
        val parts = link.split('|')
        (parts.tail.head, parts.head)
      }
      case image if image.endsWith(".png") => {
        val link = image match {
          case full if full.startsWith("http://") => full
          case absolute if absolute.startsWith("/") => resources + absolute
          case relative => resources + "/" + relativePath.map(_.getPath + "/").getOrElse("") + relative
        }
        (link, """<img src="""" + link + """"/>""")
      }
      case link => {
        (link, link)
      }
    }

    val links = new LinkRenderer {
      override def render(node: WikiLinkNode) = {
        val (href, text) = link(node.getText)
        new LinkRenderer.Rendering(href, text)
      }
    }

    // Markdown parser
    val processor = new PegDownProcessor(Extensions.ALL, PegDownPlugins.builder()
      .withPlugin(classOf[CodeReferenceParser])
      .withPlugin(classOf[VariableParser], PlayVersionVariableName)
      .build)

    // ToHtmlSerializer's are stateful and so not reusable
    def htmlSerializer = new ToHtmlSerializer(links,
      Collections.singletonMap(VerbatimSerializer.DEFAULT,
        new VerbatimSerializerWrapper(PrettifyVerbatimSerializer)),
      Arrays.asList[ToHtmlSerializerPlugin](
        new CodeReferenceSerializer(relativePath.map(_.getPath + "/").getOrElse("")),
        new VariableSerializer(Map(PlayVersionVariableName -> FastEncoder.encode(playVersion)))
      )
    ){
      override def visit(node: CodeNode) {
        super.visit(new CodeNode(node.getText.replace(PlayVersionVariableName, playVersion)))
      }
    }

    def render(markdown: String): String = {
      val astRoot = processor.parseMarkdown(markdown.toCharArray)
      htmlSerializer.toHtml(astRoot)
    }

    block(render)
  }

  // Directives to insert code, skip code and replace code
  private val Insert = """.*###insert: (.*?)(?:###.*)?""".r
  private val SkipN = """.*###skip:\s*(\d+).*""".r
  private val Skip = """.*###skip.*""".r
  private val ReplaceNext = """.*###replace: (.*?)(?:###.*)?""".r

  private class CodeReferenceSerializer(pagePath: String) extends ToHtmlSerializerPlugin {

    // Most files will be accessed multiple times from the same markdown file, no point in opening them many times
    // so memoize them.  This cache is only per file rendered, so does not need to be thread safe.
    val repo = Memoize[String, Option[Seq[String]]] { path =>
      codeRepository.loadFile(path) { is =>
        IOUtils.readLines(is).asScala.toSeq
      }
    }

    def visit(node: Node, visitor: Visitor, printer: Printer) = node match {
      case code: CodeReferenceNode => {

        // Label is after the #, or if no #, then is the link label
        val (source, label) = code.getSource.split("#", 2) match {
          case Array(source, label) => (source, label)
          case Array(source) => (source, code.getLabel)
        }

        // The file is either relative to current page page or absolute, under the root
        val sourceFile = if (source.startsWith("/")) {
          repo(source.drop(1))
        } else {
          repo(pagePath + source)
        }

        val labelPattern = ("""\s*#\Q""" + label + """\E(\s|\z)""").r
        val segment = sourceFile.flatMap { sourceCode =>
          val notLabel = (s: String) => labelPattern.findFirstIn(s).isEmpty
          val segment = sourceCode dropWhile (notLabel) drop (1) takeWhile (notLabel)
          if (segment.isEmpty) {
            None
          } else {
            Some(segment)
          }
        }

        segment.map { segment =>

          // Calculate the indent, which is equal to the smallest indent of any line, excluding lines that only consist
          // of space characters
          val indent = segment map { line =>
            if (!line.exists(_ != ' ')) None else Some(line.indexWhere(_ != ' '))
          } reduce ((i1, i2) => (i1, i2) match {
            case (None, None) => None
            case (i, None) => i
            case (None, i) => i
            case (Some(i1), Some(i2)) => Some(math.min(i1, i2))
          }) getOrElse (0)

          // Process directives in segment
          case class State(buffer: StringBuilder = new StringBuilder, skip: Option[Int] = None) {
            def dropIndentAndAppendLine(s: String): State = {
              buffer.append(s.drop(indent)).append("\n")
              this
            }
            def appendLine(s: String): State = {
              buffer.append(s).append("\n")
              this
            }
          }
          val compiledSegment = (segment.foldLeft(State()) { (state, line) =>
            state.skip match {
              case Some(n) if (n > 1) => state.copy(skip = Some(n - 1))
              case Some(n) => state.copy(skip = None)
              case None => line match {
                case Insert(code) => state.appendLine(code)
                case SkipN(n) => state.copy(skip = Some(n.toInt))
                case Skip() => state
                case ReplaceNext(code) => state.appendLine(code).copy(skip = Some(1))
                case _ => state.dropIndentAndAppendLine(line)
              }
            }
          }).buffer /* Drop last newline */ .dropRight(1).toString()

          // Guess the type of the file
          val fileType = source.split("\\.") match {
            case withExtension if (withExtension.length > 1) => Some(withExtension.last)
            case _ => None
          }

          // And visit it
          fileType.map(t => new VerbatimNode(compiledSegment, t)).getOrElse(new VerbatimNode(compiledSegment)).accept(visitor)

          true
        } getOrElse {
          printer.print("Unable to find label " + label + " in source file " + source)
          true
        }
      }
      case _ => false
    }
  }

  private class VariableSerializer(variables: Map[String, String]) extends ToHtmlSerializerPlugin {
    def visit(node: Node, visitor: Visitor, printer: Printer) = node match {
      case variable: VariableNode => {
        new TextNode(variables.get(variable.getName).getOrElse("Unknown variable: " + variable.getName)).accept(visitor)
        true
      }
      case _ => false
    }
  }

  private class VerbatimSerializerWrapper(wrapped: VerbatimSerializer) extends VerbatimSerializer {
    def serialize(node: VerbatimNode, printer: Printer) {
      val text = node.getText.replace(PlayVersionVariableName, playVersion)
      wrapped.serialize(new VerbatimNode(text, node.getType), printer)
    }
  }
}

private class Memoize[-T, +R](f: T => R) extends (T => R) {
  import scala.collection.mutable
  private[this] val vals = mutable.Map.empty[T, R]

  def apply(x: T): R = {
    if (vals.contains(x)) {
      vals(x)
    }
    else {
      val y = f(x)
      vals + ((x, y))
      y
    }
  }
}

private object Memoize {
  def apply[T, R](f: T => R) = new Memoize(f)
}

