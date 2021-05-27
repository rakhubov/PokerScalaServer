package searchWinner

import cats.{Monad, Parallel}
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, Sync, Timer}
import cats.implicits.catsSyntaxParallelSequence
import dataBase.RequestInDB.writeGameCombination
import doobie.Transactor
import gameData.CardManipulation.numberNotEqualCard
import gameData.GameData._
import gameData.RefactorFunction._
import searchWinner.CheckCombination._
import doobie.implicits._
import cats.syntax.all._

object SearchWinner {
  def searchCombination[F[_]: ContextShift](
    listPlayers: List[PlayerDB],
    connectToDataBase: Transactor[F],
    cs: ContextShift[IO]
  )(implicit parallel: Parallel[F], sy: Sync[F]): F[ExitCode.type] =
    if (listPlayers.size > 0) {
      val player = PlayerFromPlayerDB(
        listPlayers.headOption.getOrElse(PlayerDB())
      )
      if (
        (player.playerCard ++ player.allCard).size == 9 &&
        ((player.playerCard ++ player.allCard)
          .contains(numberNotEqualCard) == false)
      ) {
        val playerRefIO: F[Ref[F, Player]] = Ref.of[F, Player](player)
        for {
          playerRef <- playerRefIO
          _ <- List(
            streetFlushRef(playerRef),
            fourCardsRef(playerRef),
            fullHouseRef(playerRef),
            flushRef(playerRef),
            streetRef(playerRef),
            setRef(playerRef),
            oneOrTwoPairRef(playerRef),
            highCardRef(playerRef)
          ).parSequence.void
          player               <- playerRef.get
          stringCombinationCard = listToString(player.cardForCombination)
          _                     = println(player)
          _ <- writeGameCombination(
            player.playerID,
            stringCombinationCard,
            player.combination
          ).transact(connectToDataBase)
          _ <- searchCombination(listPlayers.drop(1), connectToDataBase, cs)
        } yield ExitCode
      } else ExitCode.pure[F]
    } else ExitCode.pure[F]

  def searchWinner(listPlayer: List[Player]) = {
    listPlayer
      .sortBy(player =>
        (
          player.combination,
          player.cardForCombination.lift(0).getOrElse(-1),
          player.cardForCombination.lift(1).getOrElse(-1),
          player.cardForCombination.lift(2).getOrElse(-1),
          player.cardForCombination.lift(3).getOrElse(-1),
          player.cardForCombination.lift(4).getOrElse(-1)
        )
      )
      .reverse
  }

}
