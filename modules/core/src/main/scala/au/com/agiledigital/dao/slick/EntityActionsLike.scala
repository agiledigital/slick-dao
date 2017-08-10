package au.com.agiledigital.dao.slick

import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * Define basic life cycle actions for a Entity that involve.
  *
  * @tparam Entity        the type of the entity that has been persisted.
  * @tparam PendingEntity the type of the entity before it has been persisted.
  */
trait EntityActionsLike[Entity, PendingEntity] {

  protected val profile: JdbcProfile

  /** The `Entity`'s Id type */
  type Id

}

trait EntityDeleteActions[Entity] extends DeleteAction[Entity] {

  import profile.api._

  /** The `Entity`'s Id type */
  type Id

  /**
    * Delete a `Entity` by `Id`
    *
    * @return DBIO[Int] with the number of affected rows
    */
  def deleteById(id: Id)(implicit exc: ExecutionContext): DBIO[Int]

}

trait EntityQueryActions[Entity] extends QueryActions[Entity] {

  import profile.api._

  /** The `Entity`'s Id type */
  type Id

  /**
    * Finds `Entity` referenced by `Id`.
    * May fail if no `Entity` is found for passed `Id`
    *
    * @return DBIO[Entity] for the `Entity`
    */
  def findById(id: Id): DBIO[Entity]

  /**
    * Finds `Entity` referenced by `Id` optionally.
    *
    * @return DBIO[Option[Entity]] for the `Entity`
    */
  def findOptionById(id: Id): DBIO[Option[Entity]]
}
