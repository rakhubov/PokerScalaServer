package gameData

import cats.effect.IO
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import java.util.UUID

object GameData {

  val logger = Slf4jLogger.getLogger[IO]

  final case class PlayerRegistration(
      id: UUID,
      name: String,
      moneyPersonalAccount: Int
  )

  final case class GameTable(
      id: UUID,
      startGame: Boolean = false,
      idPlayer: List[UUID] = List(),
      bidForTable: Int = 0,
      dealerName: UUID = UUID.randomUUID(),
      playerInGame: List[String] = List(),
      numberOpenCard: Int = 0,
      generatedCards: Set[Int] = Set()
  )

  final case class PlayerDB(
      playerID: UUID = UUID.randomUUID(),
      tableID: UUID = UUID.randomUUID(),
      name: String = "",
      playerCard: String = "",
      tableAndPlayerCard: String = "",
      cardForCombination: String = "",
      combination: Int = 0
//      money: Int = 0,
//      playerBid: Int = 0
  )

  final case class Player(
      playerID: UUID = UUID.randomUUID(),
      name: String = "",
      playerCard: List[Int] = List(),
      allCard: Vector[Int] = Vector(),
      cardForCombination: List[Int] = List(),
      combination: Int = 0
  )

  val cardCombination =
    Map[Int, String](
      0 -> "Combination not found",
      1 -> "High Card",
      2 -> "Pair",
      3 -> "Two Pair",
      4 -> "Set",
      5 -> "Street",
      6 -> "Flush",
      7 -> "Full House",
      8 -> "Four Cards",
      9 -> "Street Flash"
    )

  val cardPower =
    Map[Int, String](
      0 -> "of Twos",
      4 -> "of Triples",
      8 -> "of Fours",
      12 -> "of Fives",
      16 -> "of Sixes",
      20 -> "of Sevens",
      24 -> "of Eights",
      28 -> "of Nines",
      32 -> "of Tens",
      36 -> "of Jacks",
      40 -> "of Queens",
      44 -> "of Kings",
      48 -> "of Aces"
    )

  val powerKicker =
    Map[Int, String](
      0 -> "Two",
      4 -> "Triple",
      8 -> "Four",
      12 -> "Five",
      16 -> "Six",
      20 -> "Seven",
      24 -> "Eight",
      28 -> "Nine",
      32 -> "Ten",
      36 -> "Jack",
      40 -> "Queen",
      44 -> "King",
      48 -> "Ace"
    )

  val cardIntToString =
    Map[Int, String](
      51 -> "Ace spades",
      50 -> "Ace hearts",
      49 -> "Ace diamonds",
      48 -> "Ace clubs",
      47 -> "King spades",
      46 -> "King hearts",
      45 -> "King diamonds",
      44 -> "King clubs",
      43 -> "Queen spades",
      42 -> "Queen hearts",
      41 -> "Queen diamonds",
      40 -> "Queen clubs",
      39 -> "Jack spades",
      38 -> "Jack hearts",
      37 -> "Jack diamonds",
      36 -> "Jack clubs",
      35 -> "10 spades",
      34 -> "10 hearts",
      33 -> "10 diamonds",
      32 -> "10 clubs",
      31 -> "9 spades",
      30 -> "9 hearts",
      29 -> "9 diamonds",
      28 -> "9 clubs",
      27 -> "8 spades",
      26 -> "8 hearts",
      25 -> "8 diamonds",
      24 -> "8 clubs",
      23 -> "7 spades",
      22 -> "7 hearts",
      21 -> "7 diamonds",
      20 -> "7 clubs",
      19 -> "6 spades",
      18 -> "6 hearts",
      17 -> "6 diamonds",
      16 -> "6 clubs",
      15 -> "5 spades",
      14 -> "5 hearts",
      13 -> "5 diamonds",
      12 -> "5 clubs",
      11 -> "4 spades",
      10 -> "4 hearts",
      9 -> "4 diamonds",
      8 -> "4 clubs",
      7 -> "3 spades",
      6 -> "3 hearts",
      5 -> "3 diamonds",
      4 -> "3 clubs",
      3 -> "2 spades",
      2 -> "2 hearts",
      1 -> "2 diamonds",
      0 -> "2 clubs"
    )

//  val cardIntToString =
//    Map[Int, String](
//      51 -> "Ace spades",
//      50 -> "King spades",
//      49 -> "Queen spades",
//      48 -> "jack spades",
//      47 -> "10 spades",
//      46 -> "9 spades",
//      45 -> "8 spades",
//      44 -> "7 spades",
//      43 -> "6 spades",
//      42 -> "5 spades",
//      41 -> "4 spades",
//      40 -> "3 spades",
//      39 -> "2 spades",
//      38 -> "Ace hearts",
//      37 -> "King hearts",
//      36 -> "Queen hearts",
//      35 -> "jack hearts",
//      34 -> "10 hearts",
//      33 -> "9 hearts",
//      32 -> "8 hearts",
//      31 -> "7 hearts",
//      30 -> "6 hearts",
//      29 -> "5 hearts",
//      28 -> "4 hearts",
//      27 -> "3 hearts",
//      26 -> "2 hearts",
//      25 -> "Ace diamonds",
//      24 -> "King diamonds",
//      23 -> "Queen diamonds",
//      22 -> "jack diamonds",
//      21 -> "10 diamonds",
//      20 -> "9 diamonds",
//      19 -> "8 diamonds",
//      18 -> "7 diamonds",
//      17 -> "6 diamonds",
//      16 -> "5 diamonds",
//      15 -> "4 diamonds",
//      14 -> "3 diamonds",
//      13 -> "2 diamonds",
//      12 -> "Ace clubs",
//      11 -> "King clubs",
//      10 -> "Queen clubs",
//      9 -> "jack clubs",
//      8 -> "10 clubs",
//      7 -> "9 clubs",
//      6 -> "8 clubs",
//      5 -> "7 clubs",
//      4 -> "6 clubs",
//      3 -> "5 clubs",
//      2 -> "4 clubs",
//      1 -> "3 clubs",
//      0 -> "2 clubs"
//    )
}
