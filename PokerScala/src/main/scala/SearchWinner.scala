import GameData._
import RefactorFunction._
import CardManipulation.numberNotEqualCard
import RequestInDB.{playerSitsAtTable, writeAllPlayerCard, writeGameCombination}

import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import cats.effect.concurrent._
import cats.effect.{Concurrent, ExitCode, IO, IOApp}
import doobie.Transactor
import doobie.implicits._
import io.chrisdavenport.fuuid.FUUID
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import CheckCombination._
import Main.contextShift
import cats.Parallel
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.implicits.{catsSyntaxMonadErrorRethrow, catsSyntaxParallelSequence}

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import java.util.UUID

object SearchWinner {

  def searchCombination(
      listPlayers: List[PlayerDB],
      connectToDataBase: Transactor[IO]
  )(implicit parallel: Parallel[IO]): IO[ExitCode] =
    if (listPlayers.size > 0) {
      val player = PlayerFromPlayerDB(
        listPlayers.headOption.getOrElse(PlayerDB())
      )
      if (
        (player.playerCard ++ player.allCard).size == 9 &&
        ((player.playerCard ++ player.allCard)
          .contains(numberNotEqualCard) == false)
      ) {
        val playerRefIO: IO[Ref[IO, Player]] = Ref.of[IO, Player](player)
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
          player <- playerRef.get
          stringCombinationCard = listToString(player.cardForCombination)
          _ = println(player)
          _ <- writeGameCombination(
            player.playerID,
            stringCombinationCard,
            player.combination
          ).transact(connectToDataBase)
          _ <- searchCombination(listPlayers.tail, connectToDataBase)
        } yield (ExitCode.Success)
      } else IO(ExitCode.Success)
    } else IO(ExitCode.Success)

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
