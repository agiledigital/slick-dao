package au.com.agiledigital.dao.slick.test

import au.com.agiledigital.dao.slick.JdbcProfileProvider
import au.com.agiledigital.dao.slick.JdbcProfileProvider.H2ProfileProvider
import JdbcProfileProvider.H2ProfileProvider
import org.scalatest.Suite

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

trait H2Suite extends DbSuite with H2ProfileProvider {
  self: Suite =>

  import profile.api._

  def createSchemaAction: DBIO[Unit] = DBIO.successful(())

  def timeout = 5 seconds

  override def setupDb: profile.backend.DatabaseDef = {
    // each test suite gets its own isolated DB
    val dbUrl = s"jdbc:h2:mem:${this.getClass.getSimpleName};DB_CLOSE_DELAY=-1"
    val db = Database.forURL(dbUrl)
    val result = db.run(createSchemaAction.transactionally)
    Await.result(result, timeout)
    db
  }
}
