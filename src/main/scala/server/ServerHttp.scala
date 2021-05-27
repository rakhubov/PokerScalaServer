package server

import cats.Parallel
import cats.effect.{ExitCode, IO, _}
import cats.implicits.toSemigroupKOps
import doobie.Transactor
import errorHanding.ErrorsResponse._
import fs2.concurrent.{Queue, Topic}
import fs2.{Pipe, Stream}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame

import scala.concurrent.ExecutionContext

object WebSocketServer {

  def run(
    connectToDataBase: Transactor[IO]
  )(implicit
    concurrent: ConcurrentEffect[IO],
    timer: Timer[IO],
    parallel: Parallel[IO]
  ): IO[ExitCode] =
    for {
      chatTopic <- Topic[IO, String]("Hello!")
      _ <-
        BlazeServerBuilder[IO](ExecutionContext.global)
          .bindHttp(port = 8080, host = "localhost")
          .withHttpApp(
            httpRoute(connectToDataBase, chatTopic, concurrent, timer, parallel)
          )
          .serve
          .compile
          .drain
    } yield ExitCode.Success

  private def httpRoute(
    connectToDataBase: Transactor[IO],
    chatTopic: Topic[IO, String],
    concurrent: ConcurrentEffect[IO],
    timer: Timer[IO],
    parallel: Parallel[IO]
  ) = {
    privateRoute(connectToDataBase)(concurrent, timer) <+>
      sharedRoute(connectToDataBase, chatTopic)(parallel)
  }.orNotFound

  //

  private def privateRoute(
    connectToDataBase: Transactor[IO]
  )(implicit concurrent: ConcurrentEffect[IO], timer: Timer[IO]) =
    HttpRoutes.of[IO] { case GET -> Root / "private" =>
      import ServerPrivateCommand.checkPrivatRequest

      val checkMessage: Pipe[IO, WebSocketFrame, WebSocketFrame] = _.evalMap { case WebSocketFrame.Text(message, _) =>
        errorHandling(checkPrivatRequest(message, connectToDataBase))
          .map(response => WebSocketFrame.Text(response))
      }

      for {
        queue <- Queue.bounded[IO, WebSocketFrame](20)
        response <- WebSocketBuilder[IO].build(
          receive = queue.enqueue,
          send = queue.dequeue.through(checkMessage)
        )
      } yield response
    }

  //
  //

  private def sharedRoute(
    connectToDataBase: Transactor[IO],
    chatTopic: Topic[IO, String]
  )(implicit parallel: Parallel[IO]): HttpRoutes[IO] = {
    HttpRoutes.of[IO] { case GET -> Root / "chat" =>
      import ServerSharedCommand.checkSharedRequest

      WebSocketBuilder[IO].build(
        receive =
          chatTopic.publish.compose[Stream[IO, WebSocketFrame]](_.evalMap { case WebSocketFrame.Text(message, _) =>
            errorHandling(checkSharedRequest(message, connectToDataBase))
          }),
        send = chatTopic.subscribe(20).map(WebSocketFrame.Text(_))
      )

    }
  }

}
