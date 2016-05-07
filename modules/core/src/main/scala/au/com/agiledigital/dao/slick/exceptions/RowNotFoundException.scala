package au.com.agiledigital.dao.slick.exceptions

class RowNotFoundException[T](notFoundRecord: T) extends DaoSlickException(s"Row not found: $notFoundRecord")
