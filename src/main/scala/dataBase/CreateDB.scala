package dataBase

import doobie.{ConnectionIO, Fragment}

object CreateDB {

  val createTablePlayerRegistrationSql: String =
    """CREATE TABLE registration (
      |  id UUID PRIMARY KEY,
      |  name VARCHAR(100) NOT NULL,
      |  moneyPersonalAccount INT);""".stripMargin

  val createTableGameTableSql: String =
    """CREATE TABLE tables (
      |  id UUID PRIMARY KEY,
      |  startGame BIT,
      |  idPlayer TEXT NOT NULL,
      |  bidForTable INT,
      |  dealerName UUID,
      |  playerInGame TEXT,
      |  numberOpenCard INT,
      |  generatedCards VARCHAR(200));""".stripMargin

  val createTablePlayerAtTableSql: String =
    """CREATE TABLE players (
      |  playerID UUID PRIMARY KEY,
      |  tableID UUID NOT NULL,
      |  name VARCHAR(100) NOT NULL,
      |  money INT,
      |  playerBid INT,
      |  playerCard VARCHAR(20),
      |  tableAndPlayerCard VARCHAR(70),
      |  cardForCombination VARCHAR(50),
      |  combination INT);""".stripMargin

  val registration = Fragment.const(createTablePlayerRegistrationSql)
  val tables       = Fragment.const(createTableGameTableSql)
  val players      = Fragment.const(createTablePlayerAtTableSql)

  def setup(): ConnectionIO[Unit] =
    for {
      _ <- registration.update.run
      _ <- tables.update.run
      _ <- players.update.run
    } yield ()
}
