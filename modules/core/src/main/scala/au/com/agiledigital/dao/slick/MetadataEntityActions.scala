package au.com.agiledigital.dao.slick

import scala.concurrent.ExecutionContext

trait MetadataEntityActions[Entity, PendingEntity, D]
    extends DefaultEntityCreationActions[Entity, PendingEntity]
    with DefaultEntityUpdateActions[Entity]
    with EntitySupport[Entity] {

  import profile.api._

  def dateCreatedLens: Lens[Entity, D]

  def lastUpdatedLens: Lens[Entity, D]

  def now: D

  override def beforeInsert(entity: Entity)(
    implicit
    exc: ExecutionContext
  ): DBIO[Entity] = {
    super.beforeInsert(entity).map { superEntity =>
      dateCreatedLens.set(superEntity, now)
    }
  }

  override def beforeUpdate(id: Id, entity: Entity)(
    implicit
    exc: ExecutionContext
  ): DBIO[Entity] = {
    super.beforeUpdate(id, entity).map { superEntity =>
      lastUpdatedLens.set(superEntity, now)
    }
  }

}
