package au.com.agiledigital.dao.slick.docexamples

import au.com.agiledigital.dao.slick.JdbcProfileProvider.H2ProfileProvider
import au.com.agiledigital.dao.slick.{ Lens, EntityActions, SoftDeleteActions }
import slick.ast.BaseTypedType
import au.com.agiledigital.dao.slick.Lens._
import slick.lifted.ProvenShape

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

object SoftDeleteExample {

  final case class Coffee(name: String, version: Long = 0L, id: Int)

  final case class PendingCoffee(name: String)

  object CoffeeRepo extends EntityActions[Coffee, PendingCoffee] with SoftDeleteActions[Coffee, PendingCoffee] with H2ProfileProvider {

    import driver.api._

    def $version(table: CoffeeTable): Rep[Long] = table.version
    def versionLens: Lens[Coffee, Long] = lens { coffee: Coffee => coffee.version } { (coffee, vers) => coffee.copy(version = vers) }

    class CoffeeTable(tag: Tag) extends Table[Coffee](tag, "COFFEE") {
      def name: Rep[String] = column[String]("NAME")
      def id: Rep[Int] = column[Id]("ID", O.PrimaryKey, O.AutoInc)
      def version: Rep[Long] = column[Long]("VERSION")
      def status: Rep[Long] = column[Long]("STATUS")
      def * : ProvenShape[Coffee] = (name, version, id) <> (Coffee.tupled, Coffee.unapply)
    }

    val baseTypedType = implicitly[BaseTypedType[Id]]
    override def baseRecordStatusTypedType: BaseTypedType[RecordStatus] = implicitly[BaseTypedType[RecordStatus]]

    type PendingEntity = PendingCoffee
    type Entity = Coffee
    type Id = Int
    type EntityTable = CoffeeTable

    override val tableQuery = TableQuery[CoffeeTable]

    def $id(table: CoffeeTable): Rep[Id] = table.id

    val idLens = lens { coffee: Coffee => coffee.id } { (coffee, id) => coffee.copy(id = id) }

    def findByName(name: String): DBIO[Seq[Coffee]] = {
      tableQuery.filter(_.name === name).result
    }

    override def entity(pendingEntity: PendingCoffee): Coffee = Coffee(pendingEntity.name, 0, -1)

    override val deleted = 0L

    override val active = 1L

    override def $recordStatus(table: CoffeeTable): Rep[RecordStatus] = table.status

    override type RecordStatus = Long
  }

  def saveAction(implicit ec: ExecutionContext): CoffeeRepo.driver.api.DBIO[Coffee] = CoffeeRepo.create(PendingCoffee("Colombia"))

}
