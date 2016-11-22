package recongate_test

import java.io.{FileNotFoundException, RandomAccessFile}

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.reflect.io.File

class HugeFileReverserTest extends WordSpec with Matchers with BeforeAndAfterAll {
  private val testInput = getClass.getResource("/abc.txt").getFile
  private val outFile = "testTemp"

  /** After all the tests done - delete the output file */
  override def afterAll(): Unit = File(outFile).delete()

  "IO stuff" should {
    "Throw appropriate exception if file not found" in {
      an[FileNotFoundException] should be thrownBy {
        val reverser = new HugeFileReverser("noSuchFile", 100, outFile)
        reverser.reverseByBuckets()
      }
      an[FileNotFoundException] should be thrownBy {
        val reverser = new HugeFileReverser(testInput, 100, "noSuchFile")
        reverser.reverseByBuckets()
      }
    }
  }


  "Single bucket reading" should {
    "read proper file slice" in {
      val reverser = new HugeFileReverser(testInput, 100, outFile)
      val raf = new RandomAccessFile(testInput, "r")
      val slice = try reverser.readSlice(raf, 7, 3) finally raf.close()
      slice shouldEqual "def"
    }

    "not cut words in slice beginning" in {
      val input = getClass.getResource("/abc.txt").getFile
      val reverser = new HugeFileReverser(input, 100, outFile)
      val raf = new RandomAccessFile(input, "r")
      val slice = try reverser.readSlice(raf, 7, 5) finally raf.close()
      slice shouldEqual " def"
    }
  }

  "Whole file reversing by buckets" should {
    "reverse file in one slice" in {
      // Reverse file
      val reverser = new HugeFileReverser(testInput, 100, outFile)
      reverser.reverseByBuckets()

      // Read the result
      val testResFile = scala.io.Source.fromFile(outFile)
      val testRes = try testResFile.mkString finally testResFile.close()
      testRes shouldEqual "abc def ghi".reverse
    }

    "reverse file in multiple slices" in {
      val reverser = new HugeFileReverser(testInput, 5, outFile)
      reverser.reverseByBuckets()

      val testResFile = scala.io.Source.fromFile(outFile)
      val testRes = try testResFile.mkString finally testResFile.close()
      testRes shouldEqual "abc def ghi".reverse
    }
  }
}
