package au.com.agiledigital.dao.slick

import java.sql.SQLException
import au.com.agiledigital.dao.slick.exceptions.StaleObjectStateException
import au.com.agiledigital.dao.slick.test.H2Suite

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest._
import slick.dbio.DBIO

class SupplierTest extends FlatSpec with H2Suite with Schema {

  behavior of "A Supplier"

  it should "be persistable" in {
    val initialCount = query(Suppliers.count)

    val supplier = PendingSupplier("Acme, Inc.")

    val savedSupplier =
      commit {
        Suppliers.create(supplier)
      }

    val countAfterSave = query(Suppliers.count)
    countAfterSave shouldBe (initialCount + 1)

    commit(Suppliers.delete(savedSupplier))

    val countAfterDelete = query(Suppliers.count)
    countAfterDelete shouldBe initialCount

  }

  it should "be versionable" in {

    val supplier = PendingSupplier("abc")

    val persistedSupp = commit(Suppliers.create(supplier))
    persistedSupp.version shouldBe 0

    // modify two versions and try to persist them
    val suppWithNewVersion = commit(Suppliers.update(persistedSupp.copy(name = "abc1")))
    suppWithNewVersion.version shouldBe 1

    intercept[StaleObjectStateException[Supplier]] {
      // supplier was persisted in the mean time, so version must be different by now
      commit(Suppliers.update(persistedSupp.copy(name = "abc2")))
    }

    // supplier with new version can be persisted again
    val suppWithNewerVersion = commit(Suppliers.update(suppWithNewVersion.copy(name = "abc")))
    suppWithNewerVersion.version shouldBe 2
  }

  it should "return an error when deleting a supplier with beers linked to it" in {

    val deleteResult =
      rollback {
        for {
          supplier <- Suppliers.create(PendingSupplier("Acme, Inc."))
          beer <- Beers.create(PendingBeer("Abc", supplier.id, 3.2))
          deleteResult <- Suppliers.delete(supplier).asTry
        } yield deleteResult
      }

    deleteResult.failure.exception shouldBe a[SQLException]
  }

  override def createSchemaAction: driver.api.DBIO[Unit] = {
    driver.api.DBIO.seq(Suppliers.createSchema, Beers.createSchema)
  }

}
