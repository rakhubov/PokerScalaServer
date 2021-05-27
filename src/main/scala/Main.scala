import cats.effect._
import doobie._
import doobie.implicits._
import dataBase.{CreateDB, DbTransactor}
import server.WebSocketServer

import java.util.UUID
import scala.concurrent.ExecutionContext

object Main extends IOApp {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  override def run(args: List[String]): IO[ExitCode] =
    DbTransactor
      .pooled[IO]
      .use { connectToDataBase =>
        for {
          _ <- CreateDB.setup().transact(connectToDataBase)
          _ <- WebSocketServer.run(connectToDataBase, cs)
        } yield ()
      }
      .as(ExitCode.Success)

}
