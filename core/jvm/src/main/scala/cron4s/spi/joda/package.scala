package cron4s.spi

import cron4s.CronField
import cron4s.expr._
import cron4s.types.IsFieldExpr

import org.joda.time.{DateTime, DateTimeFieldType}

import scalaz.Equal

/**
  * Created by alonsodomin on 11/12/2016.
  */
package object joda {
  import CronField._

  implicit val dateTimeInstance = Equal.equalA[DateTime]

  implicit object JodaTimeAdapter extends DateTimeAdapter[DateTime] {

    private[this] def mapField[F <: CronField](field: F): DateTimeFieldType = field match {
      case Second     => DateTimeFieldType.secondOfMinute()
      case Minute     => DateTimeFieldType.minuteOfHour()
      case Hour       => DateTimeFieldType.hourOfDay()
      case DayOfMonth => DateTimeFieldType.dayOfMonth()
      case Month      => DateTimeFieldType.monthOfYear()
      case DayOfWeek  => DateTimeFieldType.dayOfWeek()
    }

    override def get[F <: CronField](dateTime: DateTime, field: F): Option[Int] = {
      val jodaField = mapField(field)
      val offset = if (field == DayOfWeek) -1 else 0

      if (!dateTime.isSupported(jodaField)) None
      else Some(dateTime.get(jodaField) + offset)
    }

    override def set[F <: CronField](dateTime: DateTime, field: F, value: Int): Option[DateTime] = {
      val jodaField = mapField(field)
      val offset = if (field == DayOfWeek) 1 else 0

      if (!dateTime.isSupported(jodaField)) None
      else Some(dateTime.withField(jodaField, value + offset))
    }
  }

  implicit class JodaCronExpr(expr: CronExpr) extends ExtendedCronExpr[DateTime](expr)
  implicit class JodaExpr[E[_ <: CronField] <: Expr[_], F <: CronField]
      (expr: E[F])
      (implicit ev: IsFieldExpr[E, F])
    extends ExtendedExpr[E, F, DateTime](expr)
}