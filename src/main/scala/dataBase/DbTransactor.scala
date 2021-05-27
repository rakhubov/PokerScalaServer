package dataBase

import cats.effect.{Async, Blocker, ContextShift, Resource}
import doobie.hikari.HikariTransactor
import doobie.{ExecutionContexts, Transactor}

object DbConfig {
  val dbDriverName = "org.h2.Driver"
  val dbUrl        = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
  val dbUser       = ""
  val dbPwd        = ""
}

object DbTransactor {
  import DbConfig._

  def pooled[F[_]: ContextShift: Async]: Resource[F, Transactor[F]] =
    for {
      awaitConnect <- ExecutionContexts.fixedThreadPool[F](10)
      executeJDBC  <- Blocker[F]
      connect <- HikariTransactor.newHikariTransactor[F](
        driverClassName = dbDriverName,
        url = dbUrl,
        user = dbUser,
        pass = dbPwd,
        connectEC = awaitConnect, // await connection on this EC
        blocker = executeJDBC // execute JDBC operations on this EC
      )
    } yield connect
}
