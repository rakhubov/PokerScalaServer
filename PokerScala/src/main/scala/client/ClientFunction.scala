package client

import cats.effect.IO
import org.http4s.client.jdkhttpclient.{WSConnectionHighLevel, WSFrame}

final case object ClientFunction {
  def filterByHead(findMessage: String)(
      client: WSConnectionHighLevel[IO]
  ): IO[String] = {
    for {
      receive <- client.receive
      needMessage <- receive match {
        case Some(WSFrame.Text(message, _)) =>
          message.trim match {
            case message
                if (message
                  .split("\\s+")
                  .toList
                  .headOption
                  .getOrElse("") == findMessage) =>
              IO(message)
            case _ => filterByHead(findMessage)(client)
          }
        case _ => IO("")
      }
    } yield needMessage
  }

  def filterByHeadAndLast(headMessage: String, lastMessage: String)(
      client: WSConnectionHighLevel[IO]
  ): IO[String] = {
    for {
      receive <- client.receive
      needMessage <- receive match {
        case Some(WSFrame.Text(message, _)) =>
          message.trim match {
            case message
                if (message
                  .split("\\s+")
                  .toList
                  .headOption
                  .getOrElse("") == headMessage && message
                  .split("\\s+")
                  .toList
                  .lastOption
                  .getOrElse("") == lastMessage) =>
              IO(message)
            case _ => filterByHeadAndLast(headMessage, lastMessage)(client)
          }
        case _ => IO("")
      }
    } yield needMessage
  }

  def twoFilterPrintLast(headMessage: String, lastMessage: String)(
      client: WSConnectionHighLevel[IO]
  ): IO[String] = {
    for {
      receive <- client.receive
      needMessage <- receive match {
        case Some(WSFrame.Text(message, _)) =>
          message.trim match {
            case message
                if (message
                  .split("\\s+")
                  .toList
                  .headOption
                  .getOrElse("") == headMessage && message
                  .split("\\s+")
                  .toList
                  .lastOption
                  .getOrElse("") == lastMessage) =>
              IO(message)
            case message
                if (message
                  .split("\\s+")
                  .toList
                  .lastOption
                  .getOrElse("") == lastMessage) => {
              println(message + " \n")
              twoFilterPrintLast(headMessage, lastMessage)(client)
            }
            case _ => twoFilterPrintLast(headMessage, lastMessage)(client)
          }
        case _ => IO("")
      }
    } yield needMessage
  }

}
