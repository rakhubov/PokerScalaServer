import GameData._
import CardManipulation.numberNotEqualCard

object RefactorFunction {

  def PlayerFromPlayerDB(playerDB: PlayerDB): Player = {
    val playerCard: List[Int] =
      playerDB.playerCard
        .split("\\s+")
        .toList match {
        case c1 :: c2 :: Nil =>
          List(
            c1.toIntOption.getOrElse(numberNotEqualCard),
            c2.toIntOption.getOrElse(numberNotEqualCard)
          )
        case _ => List()
      }
    val tableAndPlayerCard: List[Int] = playerDB.tableAndPlayerCard
      .split("\\s+")
      .toList match {
      case c1 :: c2 :: c3 :: c4 :: c5 :: c6 :: c7 :: Nil =>
        List(
          c1.toIntOption.getOrElse(numberNotEqualCard),
          c2.toIntOption.getOrElse(numberNotEqualCard),
          c3.toIntOption.getOrElse(numberNotEqualCard),
          c4.toIntOption.getOrElse(numberNotEqualCard),
          c5.toIntOption.getOrElse(numberNotEqualCard),
          c6.toIntOption.getOrElse(numberNotEqualCard),
          c7.toIntOption.getOrElse(numberNotEqualCard)
        )
      case _ => List()
    }
    val cardFromCombination: List[Int] = playerDB.cardForCombination
      .split("\\s+")
      .toList match {
      case c1 :: c2 :: c3 :: c4 :: c5 :: Nil =>
        List(
          c1.toIntOption.getOrElse(numberNotEqualCard),
          c2.toIntOption.getOrElse(numberNotEqualCard),
          c3.toIntOption.getOrElse(numberNotEqualCard),
          c4.toIntOption.getOrElse(numberNotEqualCard),
          c5.toIntOption.getOrElse(numberNotEqualCard)
        )
      case _ => List()
    }
    val player =
      Player(
        playerDB.playerID,
        playerDB.name,
        playerCard,
        tableAndPlayerCard.toVector.sorted.reverse, //streetSort
        cardFromCombination,
        playerDB.combination
      )
    player match {
      case value => value
      case _     => Player()
    }
  }

  def listToString[F](vector: List[F]): String =
    vector.map(_.toString) match {
      case c1 :: c2 :: c3 :: c4 :: c5 :: Nil =>
        c1 + ' ' + c2 + ' ' + c3 + ' ' + c4 + ' ' + c5
      case _ => ""
    }

}
