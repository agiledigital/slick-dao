package au.com.agiledigital.dao.slick.test

import au.com.agiledigital.dao.slick.JdbcProfileProvider
import org.scalatest._
import slick.backend.DatabasePublisher

import scala.concurrent.duration.{ FiniteDuration, _ }
import scala.concurrent.{ Await, ExecutionContext }
import scala.language.postfixOps
import scala.util.{ Failure, Success }

trait DbSuite extends BeforeAndAfterAll with Matchers with OptionValues with TryValues {

  self: Suite with JdbcProfileProvider =>

  import driver.api._

  def setupDb: driver.backend.DatabaseDef

  private lazy val database: driver.backend.DatabaseDef = setupDb

  override protected def afterAll(): Unit = {
    database.close()
  }

  def query[T](dbAction: DBIO[T])(implicit ex: ExecutionContext, timeout: FiniteDuration = 5 seconds): T =
    runAction(dbAction)

  def stream[T](dbAction: StreamingDBIO[T, T])(implicit ex: ExecutionContext, timeout: FiniteDuration = 5 seconds): DatabasePublisher[T] = {
    database.stream(dbAction.transactionally)
  }

  def commit[T](dbAction: DBIO[T])(implicit ex: ExecutionContext, timeout: FiniteDuration = 5 seconds): T =
    runAction(dbAction.transactionally)

  def rollback[T](dbAction: DBIO[T])(implicit ex: ExecutionContext, timeout: FiniteDuration = 5 seconds): T = {

    case class RollbackException(expected: T) extends RuntimeException("rollback exception")

    val markedForRollback = dbAction.flatMap { result =>
      DBIO
        .failed(RollbackException(result))
        .map(_ => result)
    }.transactionally.asTry

    val finalAction =
      markedForRollback.map {
        case Success(result) => result
        case Failure(RollbackException(result)) => result
        case Failure(other) => throw other
      }

    runAction(finalAction)
  }

  private def runAction[T](dbAction: DBIO[T])(implicit ex: ExecutionContext, timeout: FiniteDuration): T = {
    val result = database.run(dbAction)
    Await.result(result, timeout)
  }

}