package play.doc

import org.pegdown.Printer
import org.pegdown.VerbatimSerializer
import org.pegdown.ast.VerbatimNode
import org.parboiled.common.StringUtils

/**
 * Prints verbatim nodes in such a format that Google Code Prettify will work with them
 */
object PrettifyVerbatimSerializer extends VerbatimSerializer {
  def serialize(node: VerbatimNode, printer: Printer) = {
    def printAttribute(name: String, value: String): Unit = {
      printer.print(' ').print(name).print('=').print('"').print(value).print('"')
    }

    printer
      .println()
      .print("<pre")
    printAttribute("class", "prettyprint")
    printer.print("><code")
    if (!StringUtils.isEmpty(node.getType)) {
      printAttribute("class", "language-" + node.getType)
    }
    printer.print(">")

    val text = node.getText
    // print HTML breaks for all initial newlines
    text.takeWhile(_ == '\n').foreach { _ => printer.print("<br/>") }

    printer.printEncoded(text.dropWhile(_ == '\n'))
    printer.print("</code></pre>");
  }
}
