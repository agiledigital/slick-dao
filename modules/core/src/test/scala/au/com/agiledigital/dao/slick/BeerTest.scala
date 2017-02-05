package au.com.agiledigital.dao.slick

import au.com.agiledigital.dao.slick.test.H2Suite
import org.scalatest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class BeerTest extends FlatSpec with H2Suite with Schema {

  behavior of "A Beer"

  it should "be persistable" in {
    val (supplier, beer) =
      rollback {
        for {
          supplier <- Suppliers.create(PendingSupplier("Acme, Inc."))
          beer <- Beers.create(PendingBeer("Abc", supplier.id, 3.2))
          beerSupplier <- Beers.supplier(beer)
        } yield {
          beerSupplier.value shouldBe supplier
          (supplier, beer)
        }
      }
  }

  override def createSchemaAction: profile.api.DBIO[Unit] = {
    profile.api.DBIO.seq(Suppliers.createSchema, Beers.createSchema)
  }

}
