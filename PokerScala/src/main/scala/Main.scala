import cats.effect._
import doobie._
import doobie.implicits._
import RequestInDB._
import java.util.UUID

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    DbTransactor
      .pooled[IO]
      .use { connectToDataBase =>
        for {
          _ <- CreateDB.setup().transact(connectToDataBase)
          //         id = UUID.fromString("ef8737a8-7fe1-4cfe-9739-263d70937a0d")
          //         _ <- registrationInDB(id, "Lord", 1000).transact(connectToDataBase)
          //        _ <-
//            fetchMoneyPlayerAccountByID(id)
//              .to[List]
//              .transact(connectToDataBase)
//              .map(_.foreach(println))
          _ <- WebSocketServer.run(connectToDataBase)
        } yield ()
      }
      .as(ExitCode.Success)

}
