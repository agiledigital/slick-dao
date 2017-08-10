package au.com.agiledigital.dao.slick

import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait JdbcAction {

  protected val profile: JdbcProfile

}

trait CreateAction[Model, PendingModel] extends JdbcAction {

  import profile.api._

  /**
    * Insert a PendingModel.
    *
    * @return DBIO[Model] for a `Model` as persisted in the table.
    */
  def create(pendingEntity: PendingModel)(implicit exc: ExecutionContext): DBIO[Model]

}

trait UpdateAction[Model] extends JdbcAction {

  import profile.api._

  /**
    * Update a `Model`.
    *
    * @return DBIO[Model] for a `Model` as persisted in the table.
    */
  def update(entity: Model)(implicit exc: ExecutionContext): DBIO[Model]

}

trait DeleteAction[Model] extends JdbcAction {

  import profile.api._

  /**
    * Delete a `Model`.
    *
    * @return DBIO[Int] with the number of affected rows
    */
  def delete(entity: Model)(implicit exc: ExecutionContext): DBIO[Int]

}

trait QueryActions[Model] extends JdbcAction {

  import profile.api._

  /** Returns total table count */
  def count: DBIO[Int]

  /**
    * Fetch all elements from a table.
    *
    * @param fetchSize - the number of row to fetch, defaults to 100
    * @return StreamingDBIO[Seq[Model], Model]
    */
  def fetchAll(fetchSize: Int = QueryActions.defaultFetchSize)(
    implicit
    exc: ExecutionContext
  ): StreamingDBIO[Seq[Model], Model]

}

object QueryActions {

  val defaultFetchSize: Int = 100

}

/**
  * Define basic CRUD actions.
  *
  * This trait make no assumption about the presence of an Entity and a corresponding ID.
  * Therefore it can also be used for persistence of Value Objects.
  *
  * @tparam Model        the type of the model after it has been persisted.
  * @tparam PendingModel the type of the model before it has been persisted.
  */
trait CrudActions[Model, PendingModel]
  extends CreateAction[Model, PendingModel]
  with QueryActions[Model]
  with UpdateAction[Model]
  with DeleteAction[Model]

object CrudActions {}
