package au.com.agiledigital.dao.slick

import au.com.agiledigital.dao.slick.exceptions.{ RowNotFoundException, NoRowsAffectedException }
import DBIOExtensions._
import slick.ast.BaseTypedType

import scala.concurrent.ExecutionContext
import scala.language.{ existentials, higherKinds, implicitConversions }
import scala.util.{ Failure, Success }

trait EntityActions[Entity, PendingEntity] extends EntityActionsLike[Entity, PendingEntity] {

  import driver.api._

  def baseTypedType: BaseTypedType[Id]

  protected implicit lazy val btt: BaseTypedType[Id] = baseTypedType

  type EntityTable <: Table[Entity]

  def tableQuery: TableQuery[EntityTable]

  def baseQuery: Query[EntityTable, EntityTable#TableElementType, Seq] = tableQuery

  def $id(table: EntityTable): Rep[Id]

  def idLens: Lens[Entity, Id]

  override def count: DBIO[Int] = baseQuery.size.result

  override def findById(id: Id): DBIO[Entity] =
    filterById(id).result.head

  override def findOptionById(id: Id): DBIO[Option[Entity]] =
    filterById(id).result.headOption

  override def create(pendingEntity: PendingEntity)(implicit exc: ExecutionContext): DBIO[Entity] = {
    insert(pendingEntity) flatMap { id =>
      findById(id)
    }
  }

  /**
    * Before insert interceptor method. This method is called just before record insertion.
    * The default implementation returns a successful DBIO wrapping the passed entity.
    *
    * The returned `DBIOAction` is combined with the final insert `DBIOAction`
    * and 'marked' to run on the same transaction.
    *
    * Override this method if you need to add extract validation or modify the entity before insert.
    *
    * See examples bellow:
    * {{{
    * // simple validation example
    * override def beforeInsert(foo: Foo)(implicit exc: ExecutionContext): DBIO[Foo] = {
    *    if (foo.name.trim.isEmpty) {
    *      DBIO.failed(new RuntimeException("Name can't be empty!!!")
    *    } else {
    *      DBIO.successful(foo)
    *    }
    * }
    * }}}
    *
    * {{{
    * // simple audit example
    * override def beforeInsert(foo: Foo)(implicit exc: ExecutionContext): DBIO[Foo] = {
    *    // enurrisure that created and lastUpdate fields are updated just before insert
    *    val audited = foo.copy(created = DateTime.now, lastUpdate = DateTime.now)
    *    DBIO.successful(audited)
    * }
    * }}}
    */
  def beforeInsert(entity: Entity)(implicit exc: ExecutionContext): DBIO[Entity] = {
    // default implementation does nothing
    DBIO.successful(entity)
  }

  /**
    * Before update interceptor method. This method is called just before record update.
    * The default implementation returns a successful DBIO wrapping the passed entity.
    *
    * The returned `DBIOAction` is combined with the final update `DBIOAction`
    * and 'marked' to run on the same transaction.
    *
    * Override this method if you need to add extract validation or modify the entity before update.
    *
    * See examples bellow:
    *
    * {{{
    * // simple validation example
    * override def beforeUpdate(id: Int, foo: Foo)(implicit exc: ExecutionContext): DBIO[Foo] = {
    *    findById(id).flatMap { oldFoo =>
    *      if (oldFoo.name != foo.name) {
    *        DBIO.failed(new RuntimeException("Can't modify name!!!")
    *      } else {
    *        DBIO.successful(foo)
    *      }
    *    }
    * }
    * }}}
    *
    * {{{
    * // simple audit example
    * override def beforeUpdate(id: Int, foo: Foo)(implicit exc: ExecutionContext): DBIO[Foo] = {
    *    // ensure that lastUpdate fields are updated just before update
    *    val audited = foo.copy(lastUpdate = DateTime.now)
    *    DBIO.successful(audited)
    * }
    * }}}
    */
  def beforeUpdate(id: Id, entity: Entity)(implicit exc: ExecutionContext): DBIO[Entity] = {
    // default implementation does nothing
    DBIO.successful(entity)
  }

  override def insert(pendingEntity: PendingEntity)(implicit exc: ExecutionContext): DBIO[Id] = {
    val entityToInsert = entity(pendingEntity)

    val action = beforeInsert(entityToInsert).flatMap { preparedModel =>
      tableQuery.returning(tableQuery.map($id)) += preparedModel
    }
    // beforeInsert and '+=' must run on same tx
    action.transactionally
  }

  override def fetchAll(fetchSize: Int = CrudActions.defaultFetchSize)(implicit exc: ExecutionContext): StreamingDBIO[Seq[Entity], Entity] = {
    baseQuery
      .result
      .transactionally
      .withStatementParameters(fetchSize = fetchSize)
  }

  override def update(entity: Entity)(implicit exc: ExecutionContext): DBIO[Entity] = {
    val id = idLens.get(entity)
    val action =
      for {
        preparedModel <- beforeUpdate(id, entity)
        updatedModel <- update(id, preparedModel)
      } yield updatedModel

    // beforeUpdate and update must run on same tx
    action.transactionally
  }

  protected def update(id: Id, entity: Entity)(implicit exc: ExecutionContext): DBIO[Entity] = {

    val triedUpdate = filterById(id).update(entity).mustAffectOneSingleRow.asTry

    triedUpdate.flatMap {
      case Success(_) => DBIO.successful(entity)
      case Failure(NoRowsAffectedException) => DBIO.failed(new RowNotFoundException(entity))
      case Failure(ex) => DBIO.failed(ex)
    }

  }

  override def delete(entity: Entity)(implicit exc: ExecutionContext): DBIO[Int] = {
    deleteById(idLens.get(entity))
  }

  override def deleteById(id: Id)(implicit exc: ExecutionContext): DBIO[Int] = {
    filterById(id).delete.mustAffectOneSingleRow
  }

  def filterById(id: Id): Query[EntityTable, Entity, Seq] = baseQuery.filter($id(_) === id)

}
