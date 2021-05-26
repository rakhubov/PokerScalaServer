package gameData
import gameData.RefactorFunction._
import gameData.GameData._
import org.scalatest.funsuite.AnyFunSuite

import java.util.UUID

class RefactorFunctionSpec extends AnyFunSuite {

  test("Test listToString") {
    assert(
      listToString(List()) == "",
      "Error  empty.list"
    )
    assert(
      listToString(List(2)) == "2",
      "Error one element"
    )
    assert(
      listToString(List(2, 5, 4, 9)) == "2 5 4 9",
      "Error + many element"
    )
    assert(
      listToString(List("", ' ', "f", 9)) == "f 9",
      "Error + different type "
    )
  }

  test("Test listToName") {

    assert(
      listToName(List()) == "",
      "Error  empty.list"
    )
    assert(
      listToName(List("Dima")) == "Dima",
      "Error one element"
    )
    assert(
      listToName(
        List("Lord1", "Lord2", "Lord3")
      ) == "Lord1,\nLord2,\nLord3",
      "Error + many element"
    )
  }

  test("Test create Player") {
    val id = UUID.randomUUID()
    val player = Player(id)
    assert(
      player.name == ""
    )
  }

}
