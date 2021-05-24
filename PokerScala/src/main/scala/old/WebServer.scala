package old

import cats.data.{NonEmptyList, Validated}
import cats.effect.{ExitCode, IO, IOApp, _}
import cats.implicits.toSemigroupKOps
import cats.syntax.all._
import doobie.implicits._
import doobie.implicits.legacy.localdate
import doobie.{Fragment, Fragments, Meta, Transactor}
import io.circe.generic.auto._
import org.http4s.Method.POST
import org.http4s.circe.CirceEntityCodec.{
  circeEntityDecoder,
  circeEntityEncoder
}
import cats.syntax.all._
import fs2.{Pipe, Pull, Stream}
import fs2.concurrent.{Queue, Topic}
import old.Author
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.{HttpRoutes, ParseFailure, QueryParamDecoder}

import java.time.{LocalDate, Year}
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

import scala.concurrent.ExecutionContext

object WebServer extends IOApp {

  import scala.concurrent.duration._

  private val privateRoute = HttpRoutes.of[IO] {

    case GET -> Root / "private" =>
      val echoPipe: Pipe[IO, WebSocketFrame, WebSocketFrame] =
        _.collect {
          case WebSocketFrame.Text(message, _) =>
            message.split("\\s+").toList match {
              case "range" :: Nil =>
                WebSocketFrame.Text("Hello33")

              case _ => WebSocketFrame.Text(s"$message")
            }
        }

      val s = Stream
        .awakeEvery[IO](1.second)
        .map(d => WebSocketFrame.Text("You are connected for " + d.toSeconds))

      for {
        queue <- Queue.bounded[IO, WebSocketFrame](20)
        response <- WebSocketBuilder[IO].build(
          receive = queue.enqueue.compose[Stream[IO, WebSocketFrame]](str =>
            str.merge(s)
          ),
          send = queue.dequeue.through(echoPipe).merge(s)
        )
      } yield response

  }

  private def sharedRoute(chatTopic: Topic[IO, String]) =
    HttpRoutes.of[IO] {

      case GET -> Root / "chat" =>
        WebSocketBuilder[IO].build(
          // Sink, where the incoming WebSocket messages from the client are pushed to.
          receive =
            chatTopic.publish.compose[Stream[IO, WebSocketFrame]](_.collect {
              case WebSocketFrame.Text(message, _) => message.trim
            }.pull.uncons1.flatMap {
              case Some((name, tail)) =>
                tail.map(message => s"$name: $message").pull.echo
              case None => Pull.done
            }.stream),
          send = chatTopic.subscribe(20).map(WebSocketFrame.Text(_))
        )

    }

  //
  //
  //
  private def httpApp(chatTopic: Topic[IO, String]) = {
    privateRoute <+> sharedRoute(chatTopic)
  }.orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      chatTopic <- Topic[IO, String]("Hello!")
      _ <-
        BlazeServerBuilder[IO](ExecutionContext.global)
          .bindHttp(port = 9002, host = "localhost")
          .withHttpApp(httpApp(chatTopic))
          .serve
          .compile
          .drain
    } yield ExitCode.Success
  }
}
