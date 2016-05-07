package au.com.agiledigital.dao.slick

import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * Define basic life cycle actions for a Entity that involve.
  *
  * @tparam Entity the type of the entity that has been persisted.
  * @tparam PendingEntity the type of the entity before it has been persisted.
  */
trait EntityActionsLike[Entity, PendingEntity] extends CrudActions[Entity, PendingEntity] {

  protected val driver: JdbcProfile

  import driver.api._

  /** The `Entity`'s Id type */
  type Id

  def entity(pendingEntity: PendingEntity): Entity

  /**
    * Insert a new `PendingEntity`
    * @return DBIO[Id] for the generated `Id`
    */
  def insert(entity: PendingEntity)(implicit exc: ExecutionContext): DBIO[Id]

  /**
    * Delete a `Entity` by `Id`
    * @return DBIO[Int] with the number of affected rows
    */
  def deleteById(id: Id)(implicit exc: ExecutionContext): DBIO[Int]

  /**
    * Finds `Entity` referenced by `Id`.
    * May fail if no `Entity` is found for passed `Id`
    * @return DBIO[Entity] for the `Entity`
    */
  def findById(id: Id): DBIO[Entity]

  /**
    * Finds `Entity` referenced by `Id` optionally.
    * @return DBIO[Option[Entity]] for the `Entity`
    */
  def findOptionById(id: Id): DBIO[Option[Entity]]

}
