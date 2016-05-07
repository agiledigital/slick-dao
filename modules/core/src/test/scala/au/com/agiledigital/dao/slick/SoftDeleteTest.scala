package au.com.agiledigital.dao.slick

import au.com.agiledigital.dao.slick.test.H2Suite
import JdbcProfileProvider.H2ProfileProvider
import Lens._
import org.scalatest.{ FlatSpec, OptionValues }
import slick.ast.BaseTypedType

import scala.concurrent.ExecutionContext.Implicits.global

class SoftDeleteTest
    extends FlatSpec with H2Suite with JdbcProfileProvider with OptionValues {

  behavior of "An EntityDao with soft deletion "

  it should "find an undeleted entity" in {
    rollback {
      for {
        initialCount <- Foos.count
        created <- Foos.create(PendingFoo("undeleted"))
        found <- Foos.findOptionById(created.id)
        count <- Foos.count
      } yield {
        count shouldBe (initialCount + 1)
        found.value.name shouldBe "undeleted"
        found.value.id shouldBe created.id
      }
    }
  }

  it should "not find a deleted entity" in {
    rollback {
      for {
        initialCount <- Foos.count
        created <- Foos.create(PendingFoo("deleted"))
        deleted <- Foos.delete(created)
        found <- Foos.findOptionById(created.id)
        count <- Foos.count
      } yield {
        count shouldBe initialCount
        found shouldBe None
      }
    }
  }

  override def createSchemaAction: Foos.driver.api.DBIO[Unit] = {
    Foos.createSchema
  }

  case class Foo(name: String, id: Int)

  case class PendingFoo(name: String)

  class FooDao extends EntityActions[Foo, PendingFoo] with SoftDeleteActions[Foo, PendingFoo] with H2ProfileProvider {

    import driver.api._

    val baseTypedType: BaseTypedType[Id] = implicitly[BaseTypedType[Id]]

    type EntityTable = FooTable
    type PendingEntity = PendingFoo
    type Entity = Foo
    type Id = Int

    class FooTable(tag: Tag) extends Table[Foo](tag, "FOO_SOFT_DELETE_TEST") {

      def name = column[String]("NAME")

      def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

      def status = column[Long]("STATUS")

      def * = (name, id) <> (Foo.tupled, Foo.unapply)

    }

    override val tableQuery = TableQuery[FooTable]

    def $id(table: FooTable) = table.id

    val idLens = lens { foo: Foo => foo.id } { (entry, id) => entry.copy(id = id) }

    def createSchema: DBIO[Unit] = {
      import driver.api._
      sqlu"""create table FOO_SOFT_DELETE_TEST(
          ID BIGSERIAL,
          NAME varchar not null,
          STATUS BIGINT NOT NULL DEFAULT 1)""".map(i => ())
    }

    override def entity(pendingEntity: PendingFoo): Foo = Foo(pendingEntity.name, -1)

    override def baseRecordStatusTypedType: BaseTypedType[RecordStatus] = implicitly[BaseTypedType[RecordStatus]]

    override def deleted: RecordStatus = 0L

    override def active: RecordStatus = 1L

    override def $recordStatus(table: FooTable): Rep[RecordStatus] = table.status

    override type RecordStatus = Long
  }

  val Foos = new FooDao

}
