package client

import cats.effect.{ExitCode, IO, IOApp, Resource}
import io.chrisdavenport.fuuid.FUUID
import org.http4s.client.jdkhttpclient.{JdkWSClient, WSFrame, WSRequest}
import org.http4s.implicits.http4sLiteralsSyntax
import ClientFunction._

import java.net.http.HttpClient
import java.util.UUID

object Client3 extends IOApp {
  private val uriPrivate = uri"ws://localhost:8080/private"
  private val uriChat = uri"ws://localhost:8080/chat"
  private val name = "Client_3"

  private def printLine(string: String = ""): IO[Unit] = IO(println(string))

  override def run(args: List[String]): IO[ExitCode] = {
    val clientPrivateResource = Resource
      .eval(IO(HttpClient.newHttpClient()))
      .flatMap(JdkWSClient[IO](_).connectHighLevel(WSRequest(uriPrivate)))

    val clientSharedResource = Resource
      .eval(IO(HttpClient.newHttpClient()))
      .flatMap(JdkWSClient[IO](_).connectHighLevel(WSRequest(uriChat)))

    //
    //
    val playerID1 = clientPrivateResource.use { client =>
      for {
        _ <- client.send(WSFrame.Text(s"registration $name 3000"))
        receive <- client.receive
        idString = receive match {
          case Some(WSFrame.Text(message, _)) =>
            message.split("\\s+").toList.lastOption.getOrElse("").trim
          case _ => ""
        }
        _ = receive match {
          case Some(WSFrame.Text(message, _)) =>
            println(message + "\n")
          case _ => println()
        }
        id = FUUID.fromString(idString) match {
          case Right(value) => UUID.fromString(value.toString)
          case _            => UUID.randomUUID()
        }
        //        _ = Thread.sleep(1000)
      } yield id
    }

    val playerTableID =
      clientSharedResource.use { client =>
        for {
          playerID <- playerID1
          _ <- client.send(WSFrame.Text(s"game $playerID 10 300"))
          satDown <- filterByHead(name)(client)
          tableID = satDown.split("\\s+").toList.lastOption.getOrElse("")
          _ = println(satDown + " \n")
          //          _ <- client.send(WSFrame.Text(s"start $playerID"))
          start <- twoFilterPrintLast("Game", tableID)(client)
          _ = println(start.replaceFirst(tableID, "") + " \n")
        } yield (playerID, tableID)
      }

    clientPrivateResource.use { client =>
      for {
        id <- playerTableID
        playerID = id._1
        _ <- client.send(WSFrame.Text(s"MyCard $playerID"))
        receive <- client.receive
        _ = receive match {
          case Some(WSFrame.Text(message, _)) =>
            println(message + "\n")
          case _ => println()
        }
        _ <- client.send(WSFrame.Text(s"TableCard $playerID"))
        receive <- client.receive
        _ = receive match {
          case Some(WSFrame.Text(message, _)) =>
            println(message + "\n")
          case _ => println()
        }
        _ <- client.send(WSFrame.Text(s"myCombination $playerID"))
        receive <- client.receive
        _ = receive match {
          case Some(WSFrame.Text(message, _)) =>
            println(message + "\n")
          case _ => println()
        }
        _ <- client.send(WSFrame.Text(s"fetchWinner $playerID"))
        receive <- client.receive
        _ = receive match {
          case Some(WSFrame.Text(message, _)) =>
            println(message + "\n")
          case _ => println()
        }
      } yield ExitCode.Success
    }
  }
}
