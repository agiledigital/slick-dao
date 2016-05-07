package au.com.agiledigital.dao.slick.docexamples

import au.com.agiledigital.dao.slick.EntityActions
import au.com.agiledigital.dao.slick.JdbcProfileProvider.H2ProfileProvider
import au.com.agiledigital.dao.slick.Lens._
import slick.ast.BaseTypedType
import slick.lifted.ProvenShape

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

object MappingWithActiveSlick {

  final case class Coffee(name: String, id: Int)

  final case class PendingCoffee(name: String)

  object CoffeeRepo extends EntityActions[Coffee, PendingCoffee] with H2ProfileProvider {

    import driver.api._

    val baseTypedType = implicitly[BaseTypedType[Id]]

    type PendingEntity = PendingCoffee
    type Entity = Coffee
    type Id = Int
    type EntityTable = CoffeeTable

    val tableQuery = TableQuery[CoffeeTable]

    def $id(table: CoffeeTable): Rep[Id] = table.id

    val idLens = lens { coffee: Coffee => coffee.id } { (coffee, id) => coffee.copy(id = id) }

    override def entity(pendingEntity: PendingCoffee): Coffee = Coffee(pendingEntity.name, -1)

    class CoffeeTable(tag: Tag) extends Table[Coffee](tag, "COFFEE") {
      def name: Rep[String] = column[String]("NAME")

      def id: Rep[Int] = column[Id]("ID", O.PrimaryKey, O.AutoInc)

      def * : ProvenShape[Coffee] = (name, id) <> (Coffee.tupled, Coffee.unapply)
    }

    def findByName(name: String): DBIO[Seq[Coffee]] = {
      tableQuery.filter(_.name === name).result
    }

  }

  def saveAction(implicit ec: ExecutionContext): CoffeeRepo.driver.api.DBIO[Coffee] = CoffeeRepo.create(PendingCoffee("Colombia"))
}
