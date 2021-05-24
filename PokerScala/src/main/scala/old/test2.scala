package old

import cats.effect.concurrent.Ref
import cats.effect.{ExitCode, IO, IOApp}
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
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import old.Author
import org.graalvm.compiler.hotspot.replacements.HotSpotReplacementsUtil.config
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Ping, Text}
import org.http4s.{HttpRoutes, ParseFailure, QueryParamDecoder}

import java.time.{LocalDate, Year}
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object UpdateExample extends IOApp {

  val logger = Slf4jLogger.getLogger[IO]

  val counterRef: IO[Ref[IO, Int]] = Ref.of[IO, Int](0)

  def inc(counterRef: Ref[IO, Int]): IO[Unit] =
    for {
      _ <- counterRef.update(_ + 1)
      counter <- counterRef.get
      _ <- logger.info(s"counter value is $counter")
    } yield ()

  val program: IO[Unit] = for {
    counter <- Ref[IO].of(0)
    _ <- List(inc(counter), inc(counter)).parSequence.void
    v <- counter.get
    _ <- logger.info(s"counter after updates $v")
  } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    program.as(ExitCode.Success)
}
