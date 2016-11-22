package recongate_test

import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

/**
  * Reverses a huge file, writing the result to target + returns the top reversed words of the file.
  *
  * =__Assumptions:__=
  *                    - Assuming the hashmap of distinct words in the file can fit in memory (otherwise counting top
  * words would need constant reading/writing to additional file).<br>
  *                    - Assuming file's size is less than 2^63^ bytes, as this is the max value of Long.<br>
  *                    - Assuming the sliceSize is bigger than any word in the file (otherwise word cutting may occur)<br>
  *                    - Assuming the file is encoded in UTF-8
  */
object Main {
  private val logger = LoggerFactory.getLogger(HugeFileReverser.getClass)

  val TOP_WORDS_NUM = 5
  val BUCKET_SIZE = 1000

  val topWordsCounter = new TopOccurrencesCounter[String](TOP_WORDS_NUM)

  /** Function to use as side effect for the file reverser: count the words */
  def addWordsToCounter(slice: String) = {
    val words = slice.split("\\s+")
    words.foreach(topWordsCounter.add)
  }

  /**
    * Reverses a huge file, writing the result to target + returns the top reversed words of the file.
    *
    * @param args 1. file name to reverse, 2. target file name
    */
  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      logger.error(s"Wrong number of parameters for the program. Got ${args.length}, expected 2.")
      throw new RuntimeException("Add parameters to the call:\n\t1. File to reverse,\t2. Target file name\n")
    }

    val sourceFile = args(0)
    val targetFile = args(1)
    logger.info(s"Start: reversing file $sourceFile into file $targetFile")

    // Perform the operation
    val reverser = new HugeFileReverser(sourceFile, BUCKET_SIZE, targetFile, addWordsToCounter)
    val status = Try(reverser.reverseByBuckets())

    // Check the status of the operation
    status match {
      case Success(_) => logger.info(s"Success! Top words in file:\n$topWordsCounter")
      case Failure(e) => logger.error("Error occurred :(", e)
        System.exit(1)
    }
  }

}
