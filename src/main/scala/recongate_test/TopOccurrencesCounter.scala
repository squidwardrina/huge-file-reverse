package recongate_test

import scala.collection.mutable.{ListBuffer, HashMap => MutableMap}

/**
  * A hash map of elements to sizes, constantly keeping track of the ones with the biggest size.
  * Getting top elements is O(1).
  *
  * @param topAmount number of top elements to hold
  * @tparam T type of key elements
  */
class TopOccurrencesCounter[T](val topAmount: Int) {
  private val map: MutableMap[T, Int] = MutableMap.empty
  private val topElements = ListBuffer.empty[(T, Int)]

  /** Adds 1 to the element's size. If new element - adds it with size 1 */
  def add(element: T): Unit = {
    val newSize = map.getOrElse(element, 0) + 1
    map.put(element, newSize)
    updateTop(element, newSize)
  }

  /** If the new element has top size - save it to tops list */
  private def updateTop(newEl: T, size: Int) = {
    val indInTop = topElements.indexWhere(_._1 == newEl)
    if (indInTop != -1) topElements(indInTop) = (newEl, size) // if already top element - update size
    else if (topElements.size < topAmount) topElements += ((newEl, size))
    else if (topElements.nonEmpty) {
      // Check if bigger than the smallest top
      val minVal = topElements.minBy(_._2)
      if (minVal._2 < size) topElements(topElements.indexOf(minVal)) = (newEl, size)
    }
  }

  /** @return the map's elements with the biggest counters. */
  def getTopElements = topElements.toList.sortBy(-_._2)

  override def toString = "Top elements:\n" +
    getTopElements.map { case (el, size) => s"\t$el ".padTo(25, '.') + s" $size" }.mkString("\n")
}
