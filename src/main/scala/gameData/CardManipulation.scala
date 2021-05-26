package gameData

import doobie.implicits._
import cats.effect.IO
import dataBase.RequestInDB._
import doobie.Transactor
import gameData.GameData._
import io.chrisdavenport.fuuid.FUUID

import java.util.UUID
import scala.util.Random

object CardManipulation {
  val numberNotEqualCard = 54
  val cardInDeck = 52

  def generationCard(
      numberCard: Int,
      acc: Set[Int] = Set(numberNotEqualCard)
  ): Set[Int] = {
    if (acc.size < numberCard + 1)
      generationCard(numberCard, acc.incl(Random.nextInt(cardInDeck)))
    else acc.excl(numberNotEqualCard)
  }

  def writePlayerCard(
      cardTable: List[Int],
      allCardInHands: List[Int],
      playersID: List[String],
      connectToDataBase: Transactor[IO]
  ): IO[Unit] =
    if (allCardInHands.size > 1) {
      val playerID =
        FUUID.fromString(playersID.headOption.getOrElse("")) match {
          case Right(value) => UUID.fromString(value.toString)
          case _            => UUID.randomUUID()
        }
      val oneCard =
        allCardInHands.headOption.getOrElse(numberNotEqualCard).toString()
      val twoCard = oneCard + ' ' + allCardInHands
        .drop(1)
        .headOption
        .getOrElse(numberNotEqualCard)
        .toString
      val stringTablePlayerCard = cardTable match {
        case c1 :: c2 :: c3 :: c4 :: c5 :: Nil =>
          c1.toString + ' ' +
            c2.toString + ' ' + c3.toString + ' ' +
            c4.toString + ' ' + c5.toString + ' ' + twoCard
        case _ => ""
      }
      for {
        _ <- writeAllPlayerCard(stringTablePlayerCard, twoCard, playerID)
          .transact(connectToDataBase)
        _ <- writePlayerCard(
          cardTable,
          allCardInHands.takeRight(allCardInHands.size - 2),
          playersID.drop(1),
          connectToDataBase
        )
      } yield ()
    } else IO.unit

  def interpretationCardCombination(
      player: Player,
      startMessage: String = "You have a"
  ): String = {
    if (player.combination == 1)
      s"$startMessage ${cardCombination getOrElse (1, "")} " +
        s"${cardIntToString getOrElse (player.cardForCombination.lift(0).getOrElse(numberNotEqualCard), "")}" +
        s" with Kickers: " +
        s" ${cardIntToString getOrElse (player.cardForCombination.lift(1).getOrElse(numberNotEqualCard), "")}, " +
        s"${cardIntToString getOrElse (player.cardForCombination.lift(2).getOrElse(numberNotEqualCard), "")}, " +
        s"${cardIntToString getOrElse (player.cardForCombination.lift(3).getOrElse(numberNotEqualCard), "")}, " +
        s"${cardIntToString getOrElse (player.cardForCombination.lift(4).getOrElse(numberNotEqualCard), "")}"
    else if (player.combination == 2)
      s"$startMessage ${cardCombination getOrElse (2, "")} " +
        s"${cardPower getOrElse (player.cardForCombination.lift(0).getOrElse(numberNotEqualCard), "")}" +
        s" with Kickers: " +
        s" ${powerKicker getOrElse (player.cardForCombination.lift(2).getOrElse(numberNotEqualCard), "")}, " +
        s"${powerKicker getOrElse (player.cardForCombination.lift(3).getOrElse(numberNotEqualCard), "")}, " +
        s"${powerKicker getOrElse (player.cardForCombination.lift(4).getOrElse(numberNotEqualCard), "")}"
    else if (player.combination == 3)
      s"$startMessage ${cardCombination getOrElse (3, "")} of " +
        s"${powerKicker getOrElse (player.cardForCombination.lift(0).getOrElse(numberNotEqualCard), "")} and " +
        s"${powerKicker getOrElse (player.cardForCombination.lift(2).getOrElse(numberNotEqualCard), "")}, " +
        s"with Kicker:  " +
        s"${powerKicker getOrElse (player.cardForCombination.lift(4).getOrElse(numberNotEqualCard), "")}"
    else if (player.combination == 4)
      s"$startMessage ${cardCombination getOrElse (4, "")} " +
        s"${cardPower getOrElse (player.cardForCombination.lift(0).getOrElse(numberNotEqualCard), "")}" +
        s" with Kickers: " +
        s" ${cardIntToString getOrElse (player.cardForCombination.lift(3).getOrElse(numberNotEqualCard), "")}, " +
        s"${cardIntToString getOrElse (player.cardForCombination.lift(4).getOrElse(numberNotEqualCard), "")}"
    else if (player.combination == 5)
      s"$startMessage ${cardCombination getOrElse (5, "")} to:  " +
        s"${powerKicker getOrElse (player.cardForCombination.lift(0).getOrElse(numberNotEqualCard), "")}"
    else if (player.combination == 6)
      s"$startMessage ${cardCombination getOrElse (6, "")} of:  " +
        s"${cardIntToString getOrElse (player.cardForCombination.lift(0).getOrElse(numberNotEqualCard), "")}, " +
        s"${cardIntToString getOrElse (player.cardForCombination.lift(1).getOrElse(numberNotEqualCard), "")}, " +
        s"${cardIntToString getOrElse (player.cardForCombination.lift(2).getOrElse(numberNotEqualCard), "")}, " +
        s"${cardIntToString getOrElse (player.cardForCombination.lift(3).getOrElse(numberNotEqualCard), "")}, " +
        s"${cardIntToString getOrElse (player.cardForCombination.lift(4).getOrElse(numberNotEqualCard), "")}"
    else if (player.combination == 7)
      s"$startMessage ${cardCombination getOrElse (7, "")} " +
        s"${cardPower getOrElse (player.cardForCombination.lift(0).getOrElse(numberNotEqualCard), "")}, and " +
        s"${cardPower getOrElse (player.cardForCombination.lift(3).getOrElse(numberNotEqualCard), "")}"
    else if (player.combination == 8)
      s"$startMessage ${cardCombination getOrElse (8, "")} " +
        s"${cardPower getOrElse (player.cardForCombination.lift(0).getOrElse(numberNotEqualCard), "")}" +
        s" with Kicker:  " +
        s"${cardIntToString getOrElse (player.cardForCombination.lift(4).getOrElse(numberNotEqualCard), "")}"
    else if (player.combination == 9)
      s"$startMessage ${cardCombination getOrElse (9, "")} to:  " +
        s"${cardIntToString getOrElse (player.cardForCombination.lift(0).getOrElse(numberNotEqualCard), "")}"
    else ""
  }

}
