package dataBase

import doobie.implicits._
import doobie.{Fragment, Meta}
import gameData.GameData.PlayerDB

import java.util.UUID

object RequestInDB {

  implicit val uuidMeta: Meta[UUID] =
    Meta[String].timap(UUID.fromString)(_.toString)

  val tables: Fragment =
    fr"SELECT id, startGame, idPlayer," ++
      fr" bidForTable, dealerName, playerInGame," ++
      fr" numberOpenCard, generatedCards FROM tables"

  val tablesID: Fragment =
    fr"SELECT id FROM tables"

  val registrationMoney: Fragment =
    fr"SELECT moneyPersonalAccount FROM registration"

  val registrationID: Fragment =
    fr"SELECT id  FROM registration"

  val playerName: Fragment =
    fr"SELECT name FROM registration"

  val addRegistration: Fragment = fr"INSERT INTO registration" ++
    fr" (id, name, moneyPersonalAccount) VALUES"

  val addTable: Fragment = fr"INSERT INTO tables" ++
    fr" (id, startGame, idPlayer, bidForTable, numberOpenCard) VALUES"

  val addPlayer: Fragment = fr"INSERT INTO players" ++
    fr" (playerID, tableID, name, money, playerCard, tableAndPlayerCard," ++
    fr"cardForCombination, combination) VALUES"

  val tableIDForPlayer: Fragment = fr"SELECT tableID FROM players"

  val playersIDFromTable: Fragment = fr"SELECT idPlayer FROM tables"

  val player: Fragment = fr"SELECT playerID, tableID," ++
    fr" name, playerCard, tableAndPlayerCard," ++
    fr" cardForCombination, combination  FROM players"

  def registrationInDB(
      id: UUID,
      name: String,
      moneyPersonalAccount: Int
  ): doobie.ConnectionIO[Int] = {
    (addRegistration ++ fr" ($id, $name, $moneyPersonalAccount)").update.run
  }

  def fetchTableByBidNotStart(bid: Int): doobie.Query0[UUID] =
    (tablesID ++ fr"WHERE bidForTable = $bid AND startGame = 0").query[UUID]

  def fetchTableByPlayerID(playerID: UUID): doobie.Query0[UUID] =
    (tableIDForPlayer ++ fr"WHERE  playerID = $playerID").query[UUID]

  def fetchListPlayerID(tableID: UUID): doobie.Query0[String] =
    (playersIDFromTable ++ fr"WHERE  id = $tableID").query[String]

  def createTable(id: UUID, validBid: Int): doobie.ConnectionIO[Int] =
    (addTable ++ fr" ($id, 0, ' ', $validBid, 0)").update.run

  def editTable(id: UUID, validBid: Int): doobie.ConnectionIO[Int] =
    fr"UPDATE tables SET bidForTable = $validBid WHERE id = $id".update.run

  def startGameForTable(tableID: UUID): doobie.ConnectionIO[Int] =
    fr"UPDATE tables SET startGame = 1 WHERE id = $tableID".update.run

  def fetchNameByID(validPlayerID: UUID): doobie.Query0[String] =
    (playerName ++ fr"WHERE id = $validPlayerID").query[String]

  def createPlayer(
      validPlayerID: UUID,
      validMoney: Int,
      tableID: UUID,
      name: String
  ): doobie.ConnectionIO[Int] =
    (addPlayer ++ fr" ($validPlayerID, $tableID, $name, $validMoney," ++
      fr" 'player card', 'all card', 'combination card', 0)").update.run

  def playerSitsAtTable(
      validPlayerID: String,
      tableID: UUID
  ): doobie.ConnectionIO[Int] =
    (fr"UPDATE tables SET idPlayer = CASE WHEN" ++
      fr" idPlayer IS NULL THEN $validPlayerID ELSE" ++
      fr" CONCAT(idPlayer, ' ', $validPlayerID) END" ++
      fr" WHERE id = $tableID").update.run

  def fetchMoneyPlayerAccountByID(playerId: UUID): doobie.Query0[Int] =
    (registrationMoney ++ fr"WHERE id = $playerId").query[Int]

  def fetchMoneyPlayerAccountAll: doobie.Query0[Int] =
    registrationMoney.query[Int]

  def fetchRegistrationAll: doobie.Query0[UUID] =
    registrationID.query[UUID]

  def writeAllPlayerCard(
      tablePlayerCard: String,
      twoCard: String,
      playerID: UUID
  ): doobie.ConnectionIO[Int] =
    (fr"UPDATE players SET playerCard = $twoCard," ++
      fr" tableAndPlayerCard = $tablePlayerCard WHERE" ++
      fr" playerID = $playerID").update.run

  def fetchPlayers(tableID: UUID): doobie.ConnectionIO[List[PlayerDB]] =
    (player ++ fr"WHERE tableID = $tableID").query[PlayerDB].to[List]

  def writeGameCombination(
      playerID: UUID,
      combinationCard: String,
      combination: Int
  ): doobie.ConnectionIO[Int] =
    (fr"UPDATE players SET cardForCombination = $combinationCard," ++
      fr" combination = $combination WHERE" ++
      fr" playerID = $playerID").update.run

  def fetchPlayerCardByID(id: UUID): doobie.Query0[String] =
    (fr"SELECT playerCard FROM players WHERE  playerID = $id")
      .query[String]

  def fetchTableCardByID(id: UUID): doobie.Query0[String] =
    (fr"SELECT tableAndPlayerCard FROM players WHERE  playerID = $id")
      .query[String]
}
