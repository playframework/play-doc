package play.doc

import java.io.{FileInputStream, File, InputStream}
import java.util.jar.JarFile
import scala.collection.JavaConverters._

/**
 * Repository for loading files
 */
trait FileRepository {
  /**
   * Load a file using the given loader
   *
   * @param path The path of the file to load
   * @param loader The loader to load the file
   * @return The file, as loaded by the loader, or None if the doesn't exist
   */
  def loadFile[A](path: String)(loader: InputStream => A): Option[A]

  /**
   * Find a file with the given name.  The repositories directory structure is searched, and the
   * path of the first file found with that name is returned.
   *
   * @param name The name of the file to find
   * @return The path of the file, or None if it couldn't be found
   */
  def findFileWithName(name: String): Option[String]
}

/**
 * Simple filesystem implementation of the FileRepository
 *
 * @param base The base dir of the file
 */
class FilesystemRepository(base: File) extends FileRepository {

  private def cleanUp[A](loader: InputStream => A) = { is: InputStream =>
    try {
      loader(is)
    } finally {
      is.close()
    }
  }

  def loadFile[A](path: String)(loader: InputStream => A) = {
    val file = new File(base, path)
    if (file.exists() && file.isFile && file.canRead) {
      val is = new FileInputStream(file)
      Some(cleanUp(loader)(is))
    } else {
      None
    }
  }

  def findFileWithName(name: String) = {
    def findFile(name: String)(dir: File): Option[File] = {
      dir.listFiles().find(file => file.isFile && file.getName.equalsIgnoreCase(name)).orElse {
        dir.listFiles().filter(_.isDirectory).collectFirst(Function.unlift(findFile(name) _))
      }
    }
    findFile(name)(base).map(_.getAbsolutePath.drop(base.getAbsolutePath.size + 1))
  }
}

/**
 * Jar file implementation of the repository
 */
class JarRepository(jarFile: JarFile, base: Option[String] = None) extends FileRepository {

  def loadFile[A](path: String)(loader: (InputStream) => A) = {
    Option(jarFile.getEntry(base.map(_ + "/" + path).getOrElse(path))).flatMap { entry =>
      Option(jarFile.getInputStream(entry))
    } map(loader)
  }

  def findFileWithName(name: String) = {
    val slashName = "/" + name
    jarFile.entries().asScala.find { entry =>
      entry.getName match {
        case n if n.length == name.length => n.equalsIgnoreCase(name)
        case n if n.length > name.length => n.takeRight(name.length + 1).equalsIgnoreCase(slashName)
        case _ => false
      }
    }.map(_.getName)
  }

  def close() = jarFile.close()
}
