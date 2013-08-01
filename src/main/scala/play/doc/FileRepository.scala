package play.doc

import java.io.{FileInputStream, File, InputStream}
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import scala.collection.JavaConverters._

/**
 * Access to file data, provided to the handler when `handleFile` is called.
 *
 * @param name The name of the file.
 * @param size The size of the file in bytes.
 * @param is A stream with the file data.
 * @param close Used by the handler to close the file when the handler is finished.
 */
case class FileHandle(name: String, size: Long, is: InputStream, close: () => Unit)

/**
 * Repository for loading files
 */
trait FileRepository {
  /**
   * Load a file using the given loader. If the file is found then the
   * file will be opened and loader will be called with its content. The
   * file will be closed automatically when loader returns a value or throws
   * an exception.
   *
   * @param path The path of the file to load
   * @param loader The loader to load the file
   * @return The file, as loaded by the loader, or None if the doesn't exist
   */
  def loadFile[A](path: String)(loader: InputStream => A): Option[A]

  /**
   * Load a file using the given handler.  If the file is found then the
   * file will be opened and handler will be called with the file's handle. The
   * handler must call the close method on the handle to ensure that the file is closed
   * properly.
   *
   * @param path The path of the file to load
   * @param handler The handler to handle the file
   * @return The file, as loaded by the loader, or None if the doesn't exist
   */
  def handleFile[A](path: String)(handler: FileHandle => A): Option[A]

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

  private def getFile(path: String): Option[File] = {
    val file = new File(base, path)
    if (file.exists() && file.isFile && file.canRead) Some(file) else None
  }

  def loadFile[A](path: String)(loader: InputStream => A) = {
    getFile(path).map { file =>
      val is = new FileInputStream(file)
      cleanUp(loader)(is)
    }
  }

  def handleFile[A](path: String)(handler: FileHandle => A) = {
    getFile(path).map { file =>
      val is = new FileInputStream(file)
      val handle = FileHandle(file.getName, file.length, is, () => is.close())
      handler(handle)
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

  private val PathSeparator = "/"

  def getEntry(path: String): Option[(ZipEntry, InputStream)] = {
    Option(jarFile.getEntry(base.map(_ + PathSeparator + path).getOrElse(path))).flatMap {
      entry => Option(jarFile.getInputStream(entry)).map(is => (entry, is))
    }
  }

  def loadFile[A](path: String)(loader: InputStream => A) = {
    getEntry(path).map { case (_, is) => loader(is) }
  }

  def handleFile[A](path: String)(handler: FileHandle => A) = {
    getEntry(path).map { case (entry, is) =>
      val handle = FileHandle(entry.getName.split(PathSeparator).last, entry.getSize, is, () => is.close())
      handler(handle)
    }
  }

  def findFileWithName(name: String) = {
    val slashName = PathSeparator + name
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
