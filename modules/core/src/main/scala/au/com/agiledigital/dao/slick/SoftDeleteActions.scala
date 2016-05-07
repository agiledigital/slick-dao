package au.com.agiledigital.dao.slick

import slick.ast.BaseTypedType

import scala.concurrent.ExecutionContext

/**
  *
  */
trait SoftDeleteActions[Entity, PendingEntity] extends EntityActions[Entity, PendingEntity] {

  import driver.api._

  def baseRecordStatusTypedType: BaseTypedType[RecordStatus]

  protected implicit lazy val recordStatusBasedType: BaseTypedType[RecordStatus] = baseRecordStatusTypedType

  type RecordStatus

  def active: RecordStatus

  def deleted: RecordStatus

  def $recordStatus(table: EntityTable): Rep[RecordStatus]

  override def deleteById(id: Id)(implicit exc: ExecutionContext): DBIO[Int] =
    filterById(id).map($recordStatus).update(deleted)

  def activeRecordsFilter(table: EntityTable): Rep[Boolean] = $recordStatus(table) === active

  override def baseQuery: Query[EntityTable, Entity, Seq] = super.baseQuery.filter(activeRecordsFilter)
}
