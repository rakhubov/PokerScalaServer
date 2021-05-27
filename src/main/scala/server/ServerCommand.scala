package server

import cats.Parallel
import cats.effect.IO
import cats.effect.concurrent.Ref
import dataBase.RequestInDB._
import doobie.Transactor
import doobie.implicits._
import gameData.CardManipulation._
import gameData.GameData._
import gameData.RefactorFunction.{PlayerFromPlayerDB, listToName, listToString}
import gameData._
import errorHanding.ErrorsResponse._
import io.chrisdavenport.fuuid.FUUID
import searchWinner.SearchWinner._

import java.util.UUID

object ServerPrivateCommand {

  def registrationForPlayer(
    message: List[String],
    connectToDataBase: Transactor[IO]
  ): IO[Either[Errors, String]] = {
    message match {
      case name :: money :: Nil =>
        money.toIntOption match {
          case Some(moneyInt) =>
            val id = UUID.randomUUID();
            for {
              _ <-
                registrationInDB(id, name, moneyInt).transact(connectToDataBase)
              response =
                s"Your registration was successful: \n- your name is: $name \n- on your account: $money \n- your id is: $id"
            } yield Right(response)
          case _ => IO(Left(FormatMoney(money)))
        }
      case _ => IO(Left(InvalidNumberParameters(listToString(message))))
    }
  }

  def fetchPlayerCards(
    playerID: String,
    connectToDataBase: Transactor[IO]
  ): IO[Either[Errors, String]] = {
    val validID = FUUID.fromString(playerID) match {
      case Right(value) => UUID.fromString(value.toString)
      case _            => UUID.randomUUID()
    }
    for {
      playerCard <-
        fetchPlayerCardByID(validID).option.transact(connectToDataBase)
      mapPlayerCard = playerCard.getOrElse("").split("\\s+").toList match {
        case card1 :: card2 :: Nil
            if (card1.toIntOption
              .getOrElse(numberNotEqualCard) != numberNotEqualCard
              && card2.toIntOption
                .getOrElse(numberNotEqualCard) != numberNotEqualCard) =>
          Right(
            "You card:  " + (cardIntToString getOrElse (card1.toInt, "")) +
              ", " + (cardIntToString getOrElse (card2.toInt, ""))
          )
        case _ => Left(InvalidPlayerID(playerID))
      }
    } yield mapPlayerCard
  }

  def fetchTableCards(
    playerID: String,
    connectToDataBase: Transactor[IO]
  ): IO[Either[Errors, String]] = {
    val validID = FUUID.fromString(playerID) match {
      case Right(value) => UUID.fromString(value.toString)
      case _            => UUID.randomUUID()
    }
    for {
      playerAndTableCard <-
        fetchTableCardByID(validID).option.transact(connectToDataBase)
      playerCard =
        playerAndTableCard
          .getOrElse("")
          .split("\\s+")
          .toList
          .map(card => card.toIntOption.getOrElse(numberNotEqualCard))
      mapPlayerCard =
        if (
          (playerCard.contains(
            numberNotEqualCard
          ) == false) && playerCard.size == 7
        ) {
          val stringCard = playerCard.map(card =>
            "\n" +
              (cardIntToString getOrElse (card, "")) + ", "
          )
          Right(
            "Table card:  " + stringCard.lift(0).getOrElse("") + stringCard
              .lift(1)
              .getOrElse("") +
              stringCard.lift(2).getOrElse("") + stringCard
                .lift(3)
                .getOrElse("") +
              stringCard.lift(4).getOrElse("")
          )
        } else Left(InvalidFetchCardByID(playerID))
    } yield mapPlayerCard
  }

  def fetchCombination(
    id: String,
    connectToDataBase: Transactor[IO]
  ): IO[Either[Errors, String]] = {
    val validID = FUUID.fromString(id) match {
      case Right(value) => UUID.fromString(value.toString)
      case _            => UUID.randomUUID()
    }
    for {
      tableID <-
        fetchTableByPlayerID(validID).option
          .transact(
            connectToDataBase
          )
      someTableID    = tableID.getOrElse(UUID.randomUUID())
      listPlayersDB <- fetchPlayers(someTableID).transact(connectToDataBase)
      listPlayer     = listPlayersDB.map(player => PlayerFromPlayerDB(player))
      player =
        listPlayer
          .find(player => player.playerID == validID)
          .getOrElse(Player())
      playerCombination = interpretationCardCombination(player)
      response =
        if (playerCombination == "") Left(InvalidPlayerID(id))
        else Right(playerCombination)
    } yield response
  }

  def fetchWinner(
    id: String,
    connectToDataBase: Transactor[IO]
  ): IO[Either[Errors, String]] = {
    val validID = FUUID.fromString(id) match {
      case Right(value) => UUID.fromString(value.toString)
      case _            => UUID.randomUUID()
    }
    for {
      tableID <-
        fetchTableByPlayerID(validID).option
          .transact(
            connectToDataBase
          )
      someTableID    = tableID.getOrElse(UUID.randomUUID())
      listPlayersDB <- fetchPlayers(someTableID).transact(connectToDataBase)
      listPlayer     = listPlayersDB.map(player => PlayerFromPlayerDB(player))
      listWinners    = searchWinner(listPlayer)
      winnerName =
        if (listWinners.headOption.getOrElse(Player()).playerID == validID)
          "YOU"
        else
          listWinners.headOption.getOrElse(Player()).name
      winner = interpretationCardCombination(
        listWinners.headOption.getOrElse(Player()),
        s"$winnerName WON with a"
      )
      validWinner =
        if (winner == 0) Left(InvalidPlayerID(id))
        else Right(winner)
    } yield validWinner
  }

  def checkPrivatRequest(
    message: String,
    connectToDataBase: Transactor[IO]
  ): IO[Either[Errors, String]] = {
    message.split("\\s+").toList match {
      case "registration" :: next =>
        registrationForPlayer(next, connectToDataBase)
      case "MyCard" :: id :: Nil    => fetchPlayerCards(id, connectToDataBase)
      case "TableCard" :: id :: Nil => fetchTableCards(id, connectToDataBase)
      case "myCombination" :: id :: Nil =>
        fetchCombination(id, connectToDataBase)
      case "fetchWinner" :: id :: Nil => fetchWinner(id, connectToDataBase)
      case _                          => IO(Left(InvalidPrivatRequest(message)))
    }
  }
}

//
//

object ServerSharedCommand {

  def tableSearch(
    message: List[String],
    connectToDataBase: Transactor[IO]
  ): IO[Either[Errors, String]] = {
    message match {
      case playerID :: bid :: money :: Nil =>
        (
          FUUID.fromString(playerID) match {
            case Right(value) => UUID.fromString(value.toString)
            case _            => UUID.randomUUID()
          },
          bid.toIntOption,
          money.toIntOption
        ) match {
          case (validPlayerID, Some(validBid), Some(validMoney)) =>
            for {
              tablesID <-
                fetchTableByBidNotStart(validBid)
                  .to[List]
                  .transact(
                    connectToDataBase
                  )
              refTableID <- Ref.of[IO, UUID](UUID.randomUUID())
              _ <- tablesID.headOption match {
                case Some(id) => {
                  refTableID.set(id).void
                }
                case _ => {
                  val id = UUID.randomUUID();
                  refTableID.set(id) *>
                    createTable(id, validBid).transact(connectToDataBase)
                }
              }
              tableID <- refTableID.get
              name <-
                fetchNameByID(validPlayerID).option
                  .transact(connectToDataBase)
              nameString = name match {
                case Some(value) => value
                case _           => "unknown"
              }
              _ <- createPlayer(
                validPlayerID,
                validMoney,
                tableID,
                nameString
              ).transact(connectToDataBase)
              _ <- playerSitsAtTable(playerID, tableID).transact(
                connectToDataBase
              )
              messageComplete =
                s"$nameString sat down at the table with: \n- bet of: $bid\n- money: $validMoney\n- table id: $tableID"
            } yield Right(messageComplete)
          case _ => IO(Left(InvalidDataType(listToName(message))))
        }
      case _ => IO(Left(InvalidNumberParameters(listToName(message))))
    }
  }

  def startGame(
    playerID: String,
    connectToDataBase: Transactor[IO]
  )(implicit parallel: Parallel[IO]): IO[Either[Errors, String]] =
    for {
      tableID <-
        fetchTableByPlayerID(FUUID.fromString(playerID) match {
          case Right(value) => UUID.fromString(value.toString)
          case _            => UUID.randomUUID()
        }).option
          .transact(
            connectToDataBase
          )
      someTableID = tableID.getOrElse(UUID.randomUUID())
      _ <- startGameForTable(someTableID)
        .transact(connectToDataBase)
      listPlayersID <-
        fetchListPlayerID(someTableID).option
          .transact(
            connectToDataBase
          )
      stringListID   = listPlayersID.getOrElse("").trim
      playersNumber  = stringListID.split("\\s+").toList.size
      numberCard     = 5 + playersNumber * 2
      allCardInGame  = generationCard(numberCard).toList
      cardTable      = allCardInGame.take(5)
      allCardInHands = allCardInGame.takeRight(numberCard - 5)
      _ <- writePlayerCard(
        cardTable,
        allCardInHands,
        stringListID.split("\\s+").toList,
        connectToDataBase
      )
      listPlayersDB <- fetchPlayers(someTableID).transact(connectToDataBase)
      stringName     = listToName(listPlayersDB.map(player => player.name))
      _             <- searchCombination(listPlayersDB, connectToDataBase)
      response =
        if (stringName == "") Left(InvalidPlayerIdOrNotSatD(playerID))
        else Right(s"Game has start with players: \n$stringName $someTableID")
    } yield response

  def checkSharedRequest(
    message: String,
    connectToDataBase: Transactor[IO]
  )(implicit parallel: Parallel[IO]): IO[Either[Errors, String]] = {
    message.split("\\s+").toList match {
      case "game" :: next =>
        tableSearch(next, connectToDataBase)
      case "start" :: id :: Nil =>
        startGame(id, connectToDataBase)
      case _ => IO(Left(InvalidSharedRequest(message)))
    }
  }
}
