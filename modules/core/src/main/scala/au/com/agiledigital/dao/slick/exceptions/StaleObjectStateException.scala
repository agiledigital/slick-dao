package au.com.agiledigital.dao.slick.exceptions

final case class StaleObjectStateException[T](staleObject: T)
  extends DaoSlickException(s"Optimistic locking error - object in stale state: $staleObject")
