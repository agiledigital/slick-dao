package au.com.agiledigital.dao.slick

import slick.jdbc.{ DerbyProfile, H2Profile, HsqldbProfile, JdbcProfile, MySQLProfile, PostgresProfile, SQLiteProfile }

trait JdbcProfileProvider {
  type JP <: JdbcProfile
  val profile: JP
}

object JdbcProfileProvider {

  trait H2ProfileProvider extends JdbcProfileProvider {
    type JP = H2Profile
    val profile: H2Profile = H2Profile
  }

  trait PostgresProfileProvider extends JdbcProfileProvider {
    type JP = PostgresProfile
    val profile = PostgresProfile
  }

  trait DerbyProfileProvider extends JdbcProfileProvider {
    type JP = DerbyProfile
    val profile = DerbyProfile
  }

  trait HsqlProfileProvider extends JdbcProfileProvider {
    type JP = HsqldbProfile
    val profile = HsqldbProfile
  }

  trait MySQLProfileProvider extends JdbcProfileProvider {
    type JP = MySQLProfile
    val profile = MySQLProfile
  }

  trait SQLLiteProfileProvider extends JdbcProfileProvider {
    type JP = SQLiteProfile
    val profile = SQLiteProfile
  }

}
