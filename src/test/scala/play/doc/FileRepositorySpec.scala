package play.doc

import org.specs2.mutable.Specification
import java.io.File
import org.apache.commons.io.IOUtils
import java.util.jar.JarFile

object FileRepositorySpec extends Specification {

  def fileFromClasspath(name: String) = new File(Thread.currentThread.getContextClassLoader.getResource(name).toURI)

  "FilesystemRepository" should {
    val repo = new FilesystemRepository(fileFromClasspath("file-placeholder").getParentFile)
    def loadFile(path: String) = repo.loadFile(path)(IOUtils.toString)
    import repo.findFileWithName

    "load a file" in {
      loadFile("example/docs/Foo.md") must beSome("Some markdown")
    }

    "return none when file not found" in {
      loadFile("example/NotFound.md") must beNone
    }

    "return none when file is a directory" in {
      loadFile("example/docs") must beNone
    }

    "find a file with a name" in {
      findFileWithName("Foo.md") must beSome("example/docs/Foo.md")
    }

    "return none when a file with a name is not found" in {
      findFileWithName("NotFound.md") must beNone
    }

    "return none when a file with a name is a directory" in {
      findFileWithName("docs") must beNone
    }
  }

  "JarRepository" should {
    def withJarRepo[T](block: JarRepository => T): T = {
      val repo = new JarRepository(new JarFile(fileFromClasspath("example-jar-repo.jar")))
      try {
        block(repo)
      } finally {
        repo.close()
      }
    }

    def loadFile(path: String) = withJarRepo(_.loadFile(path)(IOUtils.toString))
    def findFileWithName(name: String) = withJarRepo(_.findFileWithName(name))

    "load a file" in {
      loadFile("example/docs/Foo.md") must beSome("Some markdown")
    }

    "return none when file not found" in {
      loadFile("example/NotFound.md") must beNone
    }

    "return none when file is a directory" in {
      loadFile("example/docs") must beNone
    }

    "find a file with a name" in {
      findFileWithName("Foo.md") must beSome("example/docs/Foo.md")
    }

    "return none when a file with a name is not found" in {
      findFileWithName("NotFound.md") must beNone
    }

    "return none when a file with a name is a directory" in {
      findFileWithName("docs") must beNone
    }
  }
}
