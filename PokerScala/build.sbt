name := "Poker"

version := "0.1"

scalaVersion := "2.13.5"

val http4sVersion = "0.21.22"
val circeVersion = "0.13.0"
val catsVersion = "2.2.0"
val catsTaglessVersion = "0.11"
val catsEffectVersion = "2.2.0"
val doobieVersion = "0.9.0"
val log4CatsVersion = "1.1.1"
val scalaTestVersion = "3.1.0.0-RC2"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-jdk-http-client" % "0.3.6",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-optics" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.chrisdavenport" % "fuuid_2.13" % "0.5.0",
  "io.chrisdavenport" %% "log4cats-slf4j" % log4CatsVersion,
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-h2" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "org.scalatestplus" %% "scalatestplus-scalacheck" % scalaTestVersion % Test,
  "org.scalatestplus" %% "selenium-2-45" % scalaTestVersion % Test
)
