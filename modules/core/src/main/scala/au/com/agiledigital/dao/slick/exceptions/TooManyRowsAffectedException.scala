package au.com.agiledigital.dao.slick.exceptions

class TooManyRowsAffectedException(affectedRowCount: Int, expectedRowCount: Int)
  extends DaoSlickException(s"Expected $expectedRowCount row(s) affected, got $affectedRowCount instead")
