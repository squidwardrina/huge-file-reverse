package recongate_test

import org.scalatest.{Matchers, WordSpec}

class TopOccurrencesCounterTest extends WordSpec with Matchers {
  "TopMap" should {
    "return empty top elements when map is empty" in {
      val map = new TopOccurrencesCounter[String](3)
      map.getTopElements shouldBe empty
    }

    "return 1 top element if list not full" in {
      val map = new TopOccurrencesCounter[String](3)
      map.add("a")
      map.add("a")
      map.add("a")
      map.getTopElements should have size 1
    }

    "return top elements sorted correctly" in {
      val map = new TopOccurrencesCounter[String](3)
      map.add("a")
      map.add("c")
      map.add("a")
      map.add("b")
      map.add("a")
      map.add("b")

      val expectedTop = List(
        ("a", 3),
        ("b", 2),
        ("c", 1))
      map.getTopElements shouldEqual expectedTop
    }

    "return only top elements" in {
      val map = new TopOccurrencesCounter[String](3)
      map.add("a")
      map.add("c")
      map.add("c")
      map.add("a")
      map.add("b")
      map.add("d")
      map.add("b")
      map.add("e")
      map.add("f")

      val expectedTop = List(
        ("a", 2),
        ("b", 2),
        ("c", 2))
      map.getTopElements should contain theSameElementsAs expectedTop
    }
  }
}
