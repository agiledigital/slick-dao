package au.com.agiledigital.dao.slick

import slick.ast.BaseTypedType

import scala.concurrent.ExecutionContext

/**
  * Support for soft-deleting entities rather than deleting the rows.
  */
trait SoftDeleteActions[Entity] extends DefaultEntityDeleteActions[Entity] with EntitySupport[Entity] {

  import profile.api._

  def baseRecordStatusTypedType: BaseTypedType[RecordStatus]

  protected implicit lazy val recordStatusBasedType: BaseTypedType[RecordStatus] = baseRecordStatusTypedType

  /**
    * Type of the field that will indicate whether the entity has been soft-deleted.
    */
  type RecordStatus

  /**
    * Value that indicates an entity has *not* been soft-deleted.
    */
  def active: RecordStatus

  /**
    * Value that indicates an entity has been soft-deleted.
    */
  def deleted: RecordStatus

  /**
    * Column definition of the record status field in the entity.
    */
  def $recordStatus(table: EntityTable): Rep[RecordStatus]

  // Override the delete behaviour to soft-delete via an update.
  override def deleteById(id: Id)(implicit exc: ExecutionContext): DBIO[Int] = filterById(id).map($recordStatus).update(deleted)

  // Override the base query to only return entities that have not been soft-deleted.
  override def baseQuery: Query[EntityTable, Entity, Seq] = super.baseQuery.filter($recordStatus(_) === active)
}
