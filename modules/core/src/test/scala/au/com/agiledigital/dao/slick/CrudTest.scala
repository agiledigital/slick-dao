package au.com.agiledigital.dao.slick

import au.com.agiledigital.dao.slick.JdbcProfileProvider.H2ProfileProvider
import au.com.agiledigital.dao.slick.test.H2Suite
import JdbcProfileProvider.H2ProfileProvider
import org.scalatest.FlatSpec
import slick.ast.BaseTypedType
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import Lens._

class CrudTest extends FlatSpec with H2Suite with JdbcProfileProvider {

  behavior of "An EntityDao (CRUD)"

  it should "support all CRUD operations" in {
    rollback {
      for {
        // collect initial count
        initialCount <- Foos.count

        // save new entry
        savedEntry <- Foos.create(PendingFoo("Foo"))

        // count again, must be initialCount + 1
        count <- Foos.count

        // update entry
        updatedEntry <- Foos.update(savedEntry.copy(name = "Bar"))

        // find it back from DB
        found <- Foos.findById(savedEntry.id)

        // delete it
        _ <- Foos.delete(found)

        // count total one more time
        finalCount <- Foos.count
      } yield {

        // check that we can add new entry
        count shouldBe (initialCount + 1)

        // check entity properties
        savedEntry.name shouldBe "Foo"

        // found entry must be a 'Bar'
        found.name shouldBe "Bar"

        // after delete finalCount must equal initialCount
        finalCount shouldBe initialCount

        savedEntry
      }
    }
  }

  override def createSchemaAction = {
    Foos.createSchema
  }

  case class Foo(name: String, id: Int)

  case class PendingFoo(name: String)

  class FooDao extends EntityActions[Foo, PendingFoo] with H2ProfileProvider {

    import profile.api._

    val baseTypedType: BaseTypedType[Id] = implicitly[BaseTypedType[Id]]

    type PendingEntity = PendingFoo
    type Entity = Foo
    type Id = Int

    class FooTable(tag: Tag) extends Table[Foo](tag, "FOO_CRUD_TEST") {

      def name = column[String]("NAME")

      def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

      def * = (name, id) <> (Foo.tupled, Foo.unapply)
    }

    type EntityTable = FooTable
    val tableQuery = TableQuery[EntityTable]

    def $id(table: FooTable) = table.id

    val idLens = lens { foo: Foo => foo.id } { (entry, id) => entry.copy(id = id) }

    def createSchema = {
      import profile.api._
      tableQuery.schema.create
    }

    override def entity(pendingEntity: PendingFoo): Foo = Foo(pendingEntity.name, -1)
  }

  val Foos = new FooDao

}