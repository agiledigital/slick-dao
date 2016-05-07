package au.com.agiledigital.dao.slick.docexamples

import au.com.agiledigital.dao.slick.JdbcProfileProvider.H2ProfileProvider
import au.com.agiledigital.dao.slick.{ Lens, OptimisticLocking, EntityActions }
import slick.ast.BaseTypedType
import au.com.agiledigital.dao.slick.Lens._
import slick.lifted.ProvenShape

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

object OptimisticLockingExample {

  final case class Coffee(name: String, version: Long = 0, id: Int)

  final case class PendingCoffee(name: String)

  object CoffeeRepo extends EntityActions[Coffee, PendingCoffee] with OptimisticLocking[Coffee] with H2ProfileProvider {

    import driver.api._

    def $version(table: CoffeeTable): Rep[Long] = table.version
    def versionLens: Lens[Coffee, Long] = lens { coffee: Coffee => coffee.version } { (coffee, vers) => coffee.copy(version = vers) }

    class CoffeeTable(tag: Tag) extends Table[Coffee](tag, "COFFEE") {
      def name: Rep[String] = column[String]("NAME")
      def id: Rep[Int] = column[Id]("ID", O.PrimaryKey, O.AutoInc)
      def version: Rep[Long] = column[Long]("VERSION")
      def * : ProvenShape[Coffee] = (name, version, id) <> (Coffee.tupled, Coffee.unapply)
    }

    val baseTypedType = implicitly[BaseTypedType[Id]]

    type PendingEntity = PendingCoffee
    type Entity = Coffee
    type Id = Int
    type EntityTable = CoffeeTable

    val tableQuery = TableQuery[CoffeeTable]

    def $id(table: CoffeeTable): Rep[Id] = table.id

    val idLens = lens { coffee: Coffee => coffee.id } { (coffee, id) => coffee.copy(id = id) }

    def findByName(name: String): DBIO[Seq[Coffee]] = {
      tableQuery.filter(_.name === name).result
    }

    override def entity(pendingEntity: PendingCoffee): Coffee = Coffee(pendingEntity.name, 0, -1)
  }

  def saveAction(implicit ec: ExecutionContext): CoffeeRepo.driver.api.DBIO[Coffee] = CoffeeRepo.create(PendingCoffee("Colombia"))
}
