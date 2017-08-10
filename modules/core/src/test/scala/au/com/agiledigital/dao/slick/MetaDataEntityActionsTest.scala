package au.com.agiledigital.dao.slick

import java.sql.Timestamp
import java.time.{ Clock, Instant, LocalDateTime, ZoneId }

import au.com.agiledigital.dao.slick.JdbcProfileProvider.H2ProfileProvider
import au.com.agiledigital.dao.slick.Lens._
import au.com.agiledigital.dao.slick.test.H2Suite
import org.scalatest.{ FlatSpec, OptionValues }
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType

import scala.concurrent.ExecutionContext.Implicits.global

class MetaDataEntityActionsTest
    extends FlatSpec with H2Suite with JdbcProfileProvider with OptionValues {

  behavior of "An EntityDao with soft deletion "

  it should "update last updated" in {
    val firstClock = Clock.fixed(Instant.ofEpochMilli(20000), ZoneId.systemDefault())
    val dao = new FooDao(firstClock)
    val secondClock = Clock.fixed(Instant.ofEpochMilli(50000), ZoneId.systemDefault())
    val updateDao = new FooDao(secondClock)
    rollback {
      for {
        created <- dao.create(PendingFoo("foo"))
        found <- Foos.findById(created.id)
        _ <- updateDao.update(created.copy(name = "updated_foo"))
        updatedFound <- Foos.findById(created.id)
      } yield {
        created.dateCreated shouldBe LocalDateTime.now(firstClock)
        created.lastUpdated shouldBe LocalDateTime.now(firstClock)

        updatedFound.dateCreated shouldBe LocalDateTime.now(firstClock)
        updatedFound.lastUpdated shouldBe LocalDateTime.now(secondClock)
      }
    }
  }

  override def createSchemaAction: Foos.profile.api.DBIO[Unit] = {
    Foos.createSchema
  }

  case class Foo(name: String, id: Int, lastUpdated: LocalDateTime, dateCreated: LocalDateTime)

  case class PendingFoo(name: String)

  class FooDao(clock: Clock) extends EntityActions[Foo, PendingFoo] with MetadataEntityActions[Foo, PendingFoo, LocalDateTime] with H2ProfileProvider {

    import profile.api._

    val baseTypedType: BaseTypedType[Id] = implicitly[BaseTypedType[Id]]

    type EntityTable = FooTable
    type PendingEntity = PendingFoo
    type Entity = Foo
    type Id = Int

    implicit lazy val localDateTimeMapper: JdbcType[LocalDateTime] = MappedColumnType.base[LocalDateTime, Timestamp](
      localDateTime => Timestamp.valueOf(localDateTime),
      timeStamp => timeStamp.toLocalDateTime
    )

    class FooTable(tag: Tag) extends Table[Foo](tag, "FOO_SOFT_METADATA_TEST") {

      def name = column[String]("NAME")

      def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

      def lastUpdated = column[LocalDateTime]("LAST_UPDATED")

      def dateCreated = column[LocalDateTime]("DATE_CREATED")

      def * = (name, id, lastUpdated, dateCreated) <> (Foo.tupled, Foo.unapply)

    }

    override val tableQuery = TableQuery[FooTable]

    def $id(table: FooTable) = table.id

    val idLens = lens { foo: Foo => foo.id } { (entry, id) => entry.copy(id = id) }

    def createSchema: DBIO[Unit] = {
      import profile.api._
      sqlu"""create table FOO_SOFT_METADATA_TEST(
          ID BIGSERIAL,
          NAME varchar not null,
          LAST_UPDATED TIME not null,
          DATE_CREATED TIME not null)""".map(i => ())
    }

    override def now = LocalDateTime.now(clock)

    override def entity(pendingEntity: PendingFoo): Foo = Foo(pendingEntity.name, -1, LocalDateTime.now(clock), LocalDateTime.now(clock))

    override def lastUpdatedLens: Lens[Foo, LocalDateTime] = lens { foo: Foo => foo.lastUpdated } { (entry, lastUpdated) => entry.copy(lastUpdated = lastUpdated) }

    override def dateCreatedLens: Lens[Foo, LocalDateTime] = lens { foo: Foo => foo.dateCreated } { (entry, dateCreated) => entry.copy(dateCreated = dateCreated) }
  }

  val Foos = new FooDao(Clock.fixed(Instant.ofEpochMilli(10000), ZoneId.systemDefault()))

}
