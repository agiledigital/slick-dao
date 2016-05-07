package au.com.agiledigital.dao.slick.docexamples

import slick.dbio.Effect.Write
import slick.lifted
import slick.lifted.ProvenShape
import slick.profile.FixedSqlAction

import scala.language.postfixOps

object MappingWithoutActiveSlick {

  import slick.driver.H2Driver.api._

  final case class Coffee(name: String, id: Option[Int] = None)

  class CoffeeTable(tag: Tag) extends Table[Coffee](tag, "COFFEE") {
    def name: Rep[String] = column[String]("NAME")
    def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def * : ProvenShape[Coffee] = (name, id.?) <> (Coffee.tupled, Coffee.unapply)
  }

  val Coffees: lifted.TableQuery[CoffeeTable] = TableQuery[CoffeeTable]

  val coffee: Coffee = Coffee("Colombia")
  val insertAction: FixedSqlAction[Int, NoStream, Write] = Coffees.returning(Coffees.map(_.id)) += coffee
}
