package errorHanding

import cats.effect.IO

object ErrorsResponse {
  trait Errors {
    def error: String
  }
  sealed trait ClientErrors extends Errors
  sealed trait ServerErrors extends Errors

  final case class FormatMoney(message: String) extends ClientErrors {
    val error = "money have invalid format, need be number - " + message
  }
  final case class InvalidNumberParameters(message: String) extends ClientErrors {
    val error = "money have invalid format, need be number - " + message
  }
  final case class InvalidPlayerID(message: String) extends ClientErrors {
    val error = "You enter invalid player id - " + message
  }
  final case class InvalidPlayerIdOrNotSatD(message: String) extends ClientErrors {
    val error = "You enter invalid player id or not sat down at the table - " + message
  }
  final case class InvalidPrivatRequest(message: String) extends ClientErrors {
    val error =
      "You enter invalid request or invalid numbers parameters " +
        "for you request or enter request on invalid privat service - " + message
  }
  final case class InvalidSharedRequest(message: String) extends ClientErrors {
    val error =
      "You enter invalid request or invalid numbers parameters " +
        "for you request or enter request on invalid shared service - " + message
  }
  final case class InvalidDataType(message: String) extends ClientErrors {
    val error =
      "You enter invalid data not valid type - " + message
  }

  final case class InvalidFetchCardByID(message: String) extends ServerErrors {
    val error = "Invalid fetch cards from data base - " + message
  }

  def errorHandling(error: IO[Either[Errors, String]]): IO[String] = {
    error.flatMap(response =>
      response match {
        case Right(value) => IO(value)
        case Left(errors) => IO(errors.error)
      }
    )
  }
}
