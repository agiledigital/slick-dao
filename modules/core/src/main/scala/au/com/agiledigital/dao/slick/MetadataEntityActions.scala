package au.com.agiledigital.dao.slick

import java.time.{ Clock, LocalDateTime }

import scala.concurrent.ExecutionContext

trait MetadataEntityActions[Entity, PendingEntity] extends EntityActions[Entity, PendingEntity] {

  import driver.api._

  def clock: Clock

  def dateCreatedLens: Lens[Entity, LocalDateTime]

  def lastUpdatedLens: Lens[Entity, LocalDateTime]

  override def beforeInsert(entity: Entity)(implicit exc: ExecutionContext): DBIO[Entity] = {
    super.beforeInsert(entity).map { superEntity =>
      dateCreatedLens.set(entity, LocalDateTime.now(clock))
    }
  }

  override def beforeUpdate(id: Id, entity: Entity)(implicit exc: ExecutionContext): DBIO[Entity] = {
    super.beforeUpdate(id, entity).map { superEntity =>
      lastUpdatedLens.set(superEntity, LocalDateTime.now(clock))
    }
  }

}

