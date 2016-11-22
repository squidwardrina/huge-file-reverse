package recongate_test

import java.io.{PrintWriter, RandomAccessFile, Writer}
import java.nio.charset.Charset

import org.slf4j.LoggerFactory
import recongate_test.HugeFileReverser._

import scala.annotation.tailrec

/**
  * Reverses huge files by buckets & writes to target file.
  * May perform additional operation on each reversed text bucket.
  *
  * @param sourceFilename        the name of the source file
  * @param targetFilename        where the target file will be saved
  * @param bucketSize            the size of a single bucket
  * @param additionalSideEffects optional. after reversing each bucket, this function will be performed on it.
  */
class HugeFileReverser(val sourceFilename: String, val bucketSize: Int = DEFAULT_BUFFER_SIZE,
                       targetFilename: String, additionalSideEffects: (String) => Unit = (id) => Unit) {
  private val logger = LoggerFactory.getLogger(HugeFileReverser.getClass)

  /** __Main function__ */
  def reverseByBuckets(): Unit = {
    logger.info(s"Opening files: source/target - $sourceFilename/$targetFilename")
    val targetWriter = new PrintWriter(targetFilename, ENCODING)
    try {
      val raFile = new RandomAccessFile(sourceFilename, "r")
      try reverseByBuckets(raFile, targetWriter) finally raFile.close()
    }
    finally targetWriter.close()
  }

  /**
    * Main logic - tail-recursively reads the file by buckets and performs the needed operations on each bucket.
    *
    * @param raFile       random access file, open for reading
    * @param targetWriter target file, open for writing
    */
  private def reverseByBuckets(raFile: RandomAccessFile, targetWriter: Writer): Unit = {
    /** Reverse single bucket, write it & perform the needed side effect on it.
      *
      * @param stopPos position where the new slice ends ( = start of previous slice) */
    @tailrec
    def bucketReverse(stopPos: Long = raFile.length): Unit = if (stopPos > 0) {
      val bucket = readSlice(raFile, stopPos)

      val reversedBuffer = bucket.reverse
      targetWriter.write(reversedBuffer)
      additionalSideEffects(reversedBuffer)

      val nextStopPos = stopPos - bucket.length
      bucketReverse(nextStopPos)
    }

    bucketReverse()
  }

  /** Reads charsNum bytes from file, stopping at stopPos. Starting from the beginning of the first word (no word cutting).  */
  private[recongate_test] def readSlice(raFile: RandomAccessFile, stopPos: Long, charsNum: Int = bucketSize) = {
    // Find the start position in file
    val fromPos = if (stopPos - charsNum >= 0) stopPos - charsNum else 0

    // Read the defined slice
    raFile.seek(fromPos)
    val bufferArr = new Array[Byte](bucketSize)
    raFile.read(bufferArr, 0, (stopPos - fromPos).toInt)
    val slice = new String(bufferArr, Charset.forName(ENCODING)).trim

    // Discard the first word (to avoid word cutting)
    val result = if (fromPos > 0) {
      val cleared = slice.dropWhile(!_.isWhitespace)
      if (cleared.trim.nonEmpty) cleared else slice
    }
    else slice

    logger.debug(s"Reading file slice from index ${stopPos - result.length} to index $stopPos")
    result
  }
}


object HugeFileReverser {
  val DEFAULT_BUFFER_SIZE = 7
  val ENCODING = "UTF-8"
}