import CardManipulation._
import cats.effect.IO
import cats.effect.concurrent.Ref
import doobie.Transactor
import doobie.implicits._
import GameData._
import RefactorFunction.{PlayerFromPlayerDB, listToName, listToString}

import java.util.UUID
import RequestInDB._
import SearchWinner._
import cats.Parallel
import cats.implicits.catsSyntaxApplicativeId
import io.chrisdavenport.fuuid.FUUID
import org.http4s.client.jdkhttpclient.WSFrame.Text

object ServerPrivateCommand {

  def registrationForPlayer(
      message: List[String],
      connectToDataBase: Transactor[IO]
  ): IO[String] = {
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
            } yield response
          case _ => IO(s"error $message money invalid format")
        }
      case _ => IO(s"error $message data not parsing")
    }
  }

  def fetchPlayerCards(
      playerID: String,
      connectToDataBase: Transactor[IO]
  ): IO[String] = {
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
          "You card:  " + (cardIntToString getOrElse (card1.toInt, "")) +
            ", " + (cardIntToString getOrElse (card2.toInt, ""))
        case _ => s"error fetchCard $playerID"
      }
    } yield mapPlayerCard
  }

  def fetchTableCards(
      playerID: String,
      connectToDataBase: Transactor[IO]
  ): IO[String] = {
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
          "Table card:  " + stringCard.lift(0).getOrElse("") + stringCard
            .lift(1)
            .getOrElse("") +
            stringCard.lift(2).getOrElse("") + stringCard
            .lift(3)
            .getOrElse("") +
            stringCard.lift(4).getOrElse("")
        } else s"error fetchCard $playerID"
    } yield mapPlayerCard
  }

  def fetchCombination(
      id: String,
      connectToDataBase: Transactor[IO]
  ): IO[String] = {
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
      someTableID = tableID.getOrElse(UUID.randomUUID())
      listPlayersDB <- fetchPlayers(someTableID).transact(connectToDataBase)
      listPlayer = listPlayersDB.map(player => PlayerFromPlayerDB(player))
      player =
        listPlayer
          .find(player => player.playerID == validID)
          .getOrElse(Player())
      playerCombination = interpretationCardCombination(player)
    } yield playerCombination
  }

  def fetchWinner(id: String, connectToDataBase: Transactor[IO]): IO[String] = {
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
      someTableID = tableID.getOrElse(UUID.randomUUID())
      listPlayersDB <- fetchPlayers(someTableID).transact(connectToDataBase)
      listPlayer = listPlayersDB.map(player => PlayerFromPlayerDB(player))
      listWinners = searchWinner(listPlayer)
      winnerName =
        if (listWinners.headOption.getOrElse(Player()).playerID == validID)
          "YOU"
        else
          listWinners.headOption.getOrElse(Player()).name
      winner = interpretationCardCombination(
        listWinners.headOption.getOrElse(Player()),
        s"$winnerName WON with a"
      )
      //  map = "map fgfg fg fg" //s"$winner"
    } yield winner
  }

  def checkPrivatRequest(
      message: String,
      connectToDataBase: Transactor[IO]
  ): IO[String] = {
    message.split("\\s+").toList match {
      case "registration" :: next =>
        registrationForPlayer(next, connectToDataBase)
      case "MyCard" :: id :: Nil    => fetchPlayerCards(id, connectToDataBase)
      case "TableCard" :: id :: Nil => fetchTableCards(id, connectToDataBase)
      case "myCombination" :: id :: Nil =>
        fetchCombination(id, connectToDataBase)
      case "fetchWinner" :: id :: Nil => fetchWinner(id, connectToDataBase)
      case _                          => IO(s"error $message invalid private request")
    }
  }
}

//
//
//
object ServerSharedCommand {

  def tableSearch(
      message: List[String],
      connectToDataBase: Transactor[IO]
  ): IO[String] = {
    message match {
      case playerID :: bid :: money :: Nil =>
        (
          Option(UUID.fromString(playerID)),
          bid.toIntOption,
          money.toIntOption
        ) match {
          case (Some(validPlayerID), Some(validBid), Some(validMoney)) =>
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
            } yield messageComplete
          case _ => IO(s"error $message data not parsing")
        }
    }
  }

  def startGame(
      playerID: String,
      connectToDataBase: Transactor[IO]
  )(implicit parallel: Parallel[IO]): IO[String] =
    for {
      tableID <-
        fetchTableByPlayerID(UUID.fromString(playerID)).option
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
      stringListID = listPlayersID.getOrElse("").trim
      playersNumber = stringListID.split("\\s+").toList.size
      numberCard = 5 + playersNumber * 2
      allCardInGame = generationCard(numberCard).toList
      cardTable = allCardInGame.take(5)
      allCardInHands = allCardInGame.takeRight(numberCard - 5)
      _ <- writePlayerCard(
        cardTable,
        allCardInHands,
        stringListID.split("\\s+").toList,
        connectToDataBase
      )
      listPlayersDB <- fetchPlayers(someTableID).transact(connectToDataBase)
      stringName = listToName(listPlayersDB.map(player => player.name))
      _ <- searchCombination(listPlayersDB, connectToDataBase)
      response = s"Game has start with players: \n$stringName $someTableID"
    } yield response

  def checkSharedRequest(
      message: String,
      connectToDataBase: Transactor[IO]
  )(implicit parallel: Parallel[IO]): IO[String] = {
    message.split("\\s+").toList match {
      case "game" :: next =>
        tableSearch(next, connectToDataBase)
      case "start" :: id :: Nil =>
        startGame(id, connectToDataBase)
      case _ => IO("invalid request")
//logger.info(s"consumed $n")//////////////////////////////////////////////////////
    }
  }
}
