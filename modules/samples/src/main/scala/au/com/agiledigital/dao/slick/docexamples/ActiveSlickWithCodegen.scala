package au.com.agiledigital.dao.slick.docexamples

import au.com.agiledigital.dao.slick.{ EntityActions, Lens, JdbcProfileProvider }
import au.com.agiledigital.dao.slick.docexamples.codegen.Tables
import slick.ast.BaseTypedType

/**
  * Shows how to configure active-slick with a schema that was generated via slick-codegen. In this case,
  * A schema is generated on build as the object io.strongtyped.active.slick.docexamples.codegen.Tables.
  * See codegen_schema.sql for the schema that feeds into the codegen.
  */
object ActiveSlickWithCodegen {

  abstract class ComputersRepo(tables: Tables) extends EntityActions[Tables.ComputersRow, PendingComputer] with JdbcProfileProvider {
    //
    // Implement JdbcProfileProvider with JDBCProfile from generated Tables.scala
    //
    override type JP = Tables.profile.type
    // Sucks that this is necessary. Did we have to define this type in JdbcProfileProvider? Why not just use JdbcProfile?
    override val driver = Tables.profile

    //
    // Implement EntityActions
    //

    import driver.api._

    type Id = Long
    type EntityTable = Tables.Computers

    val baseTypedType = implicitly[BaseTypedType[Id]]
    val tableQuery = Tables.Computers
    val idLens: Lens[Tables.ComputersRow, Long] = {
      // For the getter, use 0L as a sentinel value because generated ID is usually non-optional
      Lens.lens { row: Tables.ComputersRow => row.id } { (row, id) => row.copy(id = id) }
    }

    override def $id(table: EntityTable): Rep[Long] = {
      table.id
    }

    override def entity(pendingEntity: PendingComputer): Tables.ComputersRow = Tables.ComputersRow(0, pendingEntity.name)

  }

  object ComputersRepo extends ComputersRepo(Tables)

  final case class PendingComputer(name: String)
}
