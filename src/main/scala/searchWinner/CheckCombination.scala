package searchWinner

import cats.effect.IO
import cats.effect.concurrent.Ref
import gameData.CardManipulation.numberNotEqualCard
import gameData.GameData._

object CheckCombination {
  def streetFlushRef[F[_]](playerRef: Ref[F, Player]): F[Unit] = {
    playerRef
      .modify(player => (streetFlush(player, 0), player))
//      .flatMap(message => logger.info("streetFlushRef " ++ message.toString))
  }

  def streetFlush(player: Player, counter: Int): Player = {
    val cardFlashSort: Vector[Int] =
      player.allCard.sortBy(cardID => (cardID % 4, cardID / 4)).reverse
    val card1 = cardFlashSort.lift(counter).getOrElse(numberNotEqualCard)
    val card2 = cardFlashSort.lift(counter + 1).getOrElse(numberNotEqualCard)
    val card3 = cardFlashSort.lift(counter + 2).getOrElse(numberNotEqualCard)
    val card4 = cardFlashSort.lift(counter + 3).getOrElse(numberNotEqualCard)
    val card5 = cardFlashSort.lift(counter + 4).getOrElse(numberNotEqualCard)
    if (
      card1 - card2 == 4 &&
      card2 - card3 == 4 &&
      card3 - card4 == 4 &&
      card4 - card5 == 4
    ) {
      player.copy(
        cardForCombination = List(card1, card2, card3, card4, card5),
        combination = 9
      )
    } else if (counter <= 2) streetFlush(player, counter + 1)
    else player
  }

  def fourCardsRef[F[_]](playerRef: Ref[F, Player]): F[Unit] = {
    playerRef
      .modify(player => (fourCards(player, 0), player))
    //     .flatMap(message => logger.info("four cards " ++ message.toString))
  }

  def fourCards(player: Player, counter: Int): Player = {
    if (player.combination < 8) {
      val cards = player.allCard.map(card => card / 4)
      searchEqualCard(cards, 4) match {
        case `numberNotEqualCard` => player
        case card => {
          val cardKicker = player.allCard
            .filter(_ / 4 != card)
            .sorted
            .reverse
            .headOption
            .getOrElse(numberNotEqualCard)
          player.copy(
            cardForCombination = List(card * 4, card * 4, card * 4, card * 4, cardKicker),
            combination = 8
          )
        }
      }
    } else player
  }

  def fullHouseRef[F[_]](playerRef: Ref[F, Player]): F[Unit] = {
    playerRef
      .modify(player => ((fullHouse(player)), player))
    //     .flatMap(message => logger.info("fullHouseRef " ++ message.toString))
  }

  def fullHouse(player: Player): Player = {
    if (player.combination < 7) {
      val cards = player.allCard.map(card => card / 4)
      searchEqualCard(cards, 3) match {
        case `numberNotEqualCard` => player
        case card => {
          val fourCard = cards
            .filter(_ != card)
            .sorted
            .reverse
          if (fourCard.size == 4) {
            searchEqualCard(fourCard, 2) match {
              case `numberNotEqualCard` => player
              case card2 =>
                player.copy(
                  cardForCombination = List(card * 4, card * 4, card * 4, card2 * 4, card2 * 4),
                  combination = 7
                )
            }
          } else player
        }
      }
    } else player
  }

  def flushRef[F[_]](playerRef: Ref[F, Player]): F[Unit] = {
    playerRef
      .modify(player => (flush(player), player))
    //   .flatMap(message => logger.info("flushRef " ++ message.toString))
  }

  def flush(player: Player): Player = {
    if (player.combination < 6) {
      val sortBySuit = player.allCard.map(card => card % 4)
      val suit =
        if (sortBySuit.count(_ == 0) > 4) 0
        else if (sortBySuit.count(_ == 1) > 4) 1
        else if (sortBySuit.count(_ == 2) > 4) 2
        else if (sortBySuit.count(_ == 3) > 4) 3
        else numberNotEqualCard

      if (suit != numberNotEqualCard) {
        val cardWithSuit = player.allCard.filter(_ % 4 == suit)
        val card1        = cardWithSuit.lift(0).getOrElse(numberNotEqualCard)
        val card2        = cardWithSuit.lift(1).getOrElse(numberNotEqualCard)
        val card3        = cardWithSuit.lift(2).getOrElse(numberNotEqualCard)
        val card4        = cardWithSuit.lift(3).getOrElse(numberNotEqualCard)
        val card5        = cardWithSuit.lift(4).getOrElse(numberNotEqualCard)
        player.copy(
          cardForCombination = List(card1, card2, card3, card4, card5),
          combination = 6
        )
      } else player
    } else player
  }

  def streetRef[F[_]](playerRef: Ref[F, Player]): F[Unit] = {
    playerRef
      .modify(player => (street(player), player))
    //   .flatMap(message => logger.info("street " ++ message.toString))
  }

  def street(player: Player): Player = {
    if (player.combination < 5) {
      val cardWithoutSuit =
        player.allCard.map(card => card / 4).distinct.sorted.reverse
      val card1 = cardWithoutSuit.lift(0).getOrElse(numberNotEqualCard)
      val card2 = cardWithoutSuit.lift(1).getOrElse(numberNotEqualCard)
      val card3 = cardWithoutSuit.lift(2).getOrElse(numberNotEqualCard)
      val card4 = cardWithoutSuit.lift(3).getOrElse(numberNotEqualCard)
      val card5 = cardWithoutSuit.lift(4).getOrElse(numberNotEqualCard)
      val card6 = cardWithoutSuit.lift(5).getOrElse(numberNotEqualCard)
      val card7 = cardWithoutSuit.lift(6).getOrElse(numberNotEqualCard)
      val card =
        if (
          card1 - card2 == 1 && card2 - card3 == 1
          && card3 - card4 == 1 && card4 - card5 == 1
        ) card1
        else if (
          card2 - card3 == 1 && card3 - card4 == 1
          && card4 - card5 == 1 && card5 - card6 == 1
        ) card2
        else if (
          card3 - card4 == 1 && card4 - card5 == 1
          && card5 - card6 == 1 && card6 - card7 == 1
        ) card3
        else numberNotEqualCard
      if (card != numberNotEqualCard) {
        player.copy(
          cardForCombination = List(card * 4, card * 4, card * 4, card * 4, card * 4),
          combination = 5
        )
      } else player
    } else player
  }

  def setRef[F[_]](playerRef: Ref[F, Player]): F[Unit] = {
    playerRef
      .modify(player => ((set(player)), player))
    //    .flatMap(message => logger.info("setRef " ++ message.toString))
  }

  def set(player: Player): Player = {
    if (player.combination < 4) {
      val cards = player.allCard.map(card => card / 4)
      searchEqualCard(cards, 3) match {
        case `numberNotEqualCard` => player
        case card => {
          val kickerCard = player.allCard
            .filter(_ / 4 != card)
            .sorted
            .reverse
          val card1 = kickerCard.lift(0).getOrElse(numberNotEqualCard)
          val card2 = kickerCard.lift(1).getOrElse(numberNotEqualCard)
          player.copy(
            cardForCombination = List(card * 4, card * 4, card * 4, card1, card2),
            combination = 4
          )
        }
      }
    } else player
  }

  def oneOrTwoPairRef[F[_]](playerRef: Ref[F, Player]): F[Unit] = {
    playerRef
      .modify(player => ((oneOrTwoPair(player)), player))
    //     .flatMap(message => logger.info("oneOrTwoPairRef " ++ message.toString))
  }

  def oneOrTwoPair(player: Player): Player = {
    if (player.combination < 3) {
      val cards = player.allCard.map(card => card / 4)
      searchEqualCard(cards, 2) match {
        case `numberNotEqualCard` => player
        case card => {
          val cardForTwoPair = cards
            .filter(_ != card)
            .sorted
            .reverse
          if (cardForTwoPair.size == 5) {
            searchEqualCard(cardForTwoPair, 2) match {
              //one Pair
              case `numberNotEqualCard` => {
                val card1 = cardForTwoPair.lift(0).getOrElse(numberNotEqualCard)
                val card2 = cardForTwoPair.lift(1).getOrElse(numberNotEqualCard)
                val card3 = cardForTwoPair.lift(2).getOrElse(numberNotEqualCard)
                player.copy(
                  cardForCombination = List(card * 4, card * 4, card1 * 4, card2 * 4, card3 * 4),
                  combination = 2
                )
              }
              //two pair
              case card2 => {
                val kickerCard = cardForTwoPair
                  .filter(_ != card2)
                  .sorted
                  .reverse
                  .headOption
                  .getOrElse(numberNotEqualCard)
                player.copy(
                  cardForCombination = List(
                    card * 4,
                    card * 4,
                    card2 * 4,
                    card2 * 4,
                    kickerCard * 4
                  ),
                  combination = 3
                )
              }
            }
          } else player
        }
      }
    } else player
  }

  def highCardRef[F[_]](playerRef: Ref[F, Player]): F[Unit] = {
    playerRef
      .modify(player => ((highCard(player)), player))
//      .flatMap(message => logger.info("highCardRef " ++ message.toString))
  }

  def highCard(player: Player): Player = {
    if (player.combination == 0) {
      val card1 = player.allCard.lift(0).getOrElse(numberNotEqualCard)
      val card2 = player.allCard.lift(1).getOrElse(numberNotEqualCard)
      val card3 = player.allCard.lift(2).getOrElse(numberNotEqualCard)
      val card4 = player.allCard.lift(3).getOrElse(numberNotEqualCard)
      val card5 = player.allCard.lift(4).getOrElse(numberNotEqualCard)
      player.copy(
        cardForCombination = List(card1, card2, card3, card4, card5),
        combination = 1
      )
    } else player
  }

  def searchEqualCard(cards: Vector[Int], number: Int): Int = {
    if (cards.size > number - 1) {
      if (
        cards
          .drop(1)
          .count(
            _ == cards.headOption.getOrElse(numberNotEqualCard)
          ) == number - 1
      )
        cards.headOption.getOrElse(numberNotEqualCard)
      else searchEqualCard(cards.drop(1), number)
    } else numberNotEqualCard
  }
}
