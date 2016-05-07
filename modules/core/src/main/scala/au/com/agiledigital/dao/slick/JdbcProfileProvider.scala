package au.com.agiledigital.dao.slick

import slick.driver.{ DerbyDriver, H2Driver, HsqldbDriver, JdbcProfile, MySQLDriver, PostgresDriver, SQLiteDriver }

trait JdbcProfileProvider {
  type JP <: JdbcProfile
  val driver: JP
}

object JdbcProfileProvider {

  trait H2ProfileProvider extends JdbcProfileProvider {
    type JP = H2Driver
    val driver: H2Driver = H2Driver
  }

  trait PostgresProfileProvider extends JdbcProfileProvider {
    type JP = PostgresDriver
    val driver = PostgresDriver
  }

  trait DerbyProfileProvider extends JdbcProfileProvider {
    type JP = DerbyDriver
    val driver = DerbyDriver
  }

  trait HsqlProfileProvider extends JdbcProfileProvider {
    type JP = HsqldbDriver
    val driver = HsqldbDriver
  }

  trait MySQLProfileProvider extends JdbcProfileProvider {
    type JP = MySQLDriver
    val driver = MySQLDriver
  }

  trait SQLLiteProfileProvider extends JdbcProfileProvider {
    type JP = SQLiteDriver
    val driver = SQLiteDriver
  }

}
