package au.com.agiledigital.dao.slick

import au.com.agiledigital.dao.slick.DBIOExtensions._
import au.com.agiledigital.dao.slick.exceptions.{ NoRowsAffectedException, StaleObjectStateException }

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait OptimisticLocking[Entity] extends DefaultEntityUpdateActions[Entity] with EntitySupport[Entity] {

  import profile.api._

  def $version(table: EntityTable): Rep[Long]

  def versionLens: Lens[Entity, Long]

  override protected def update(id: Id, versionable: Entity)(implicit exc: ExecutionContext): DBIO[Entity] = {

    // extract current version
    val currentVersion = versionLens.get(versionable)

    // build a query selecting entity with current version
    val queryByIdAndVersion = filterById(id).filter($version(_) === currentVersion)

    // model with incremented version
    val modelWithNewVersion = versionLens.set(versionable, currentVersion + 1)

    val tryUpdate = queryByIdAndVersion.update(modelWithNewVersion).mustAffectOneSingleRow.asTry

    // in case of failure, we want a more meaningful exception ie: StaleObjectStateException
    tryUpdate.flatMap {
      case Success(_) => DBIO.successful(modelWithNewVersion)
      case Failure(NoRowsAffectedException) => DBIO.failed(new StaleObjectStateException(versionable))
      case Failure(e) => DBIO.failed(e)
    }
  }

}
