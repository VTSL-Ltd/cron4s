/*
 * Copyright 2017 Antonio Alonso Dominguez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cron4s.datetime

import cron4s.{CronField, CronUnit}
import cron4s.base.{Direction, Predicate}
import cron4s.expr._

import shapeless.Coproduct

import scalaz.PlusEmpty

/**
  * Created by alonsodomin on 14/01/2017.
  */
trait DateTimeCron[T] {

  protected def matches[DateTime](expr: T, dt: IsDateTime[DateTime])
    (implicit M: PlusEmpty[Predicate]): Predicate[DateTime]

  def allOf[DateTime](expr: T, dt: IsDateTime[DateTime]): Predicate[DateTime] =
    matches(expr, dt)(Predicate.conjunction.monoidK)

  def anyOf[DateTime](expr: T, dt: IsDateTime[DateTime]): Predicate[DateTime] =
    matches(expr, dt)(Predicate.disjunction.monoidK)

  @inline
  def next[DateTime](expr: T, dt: IsDateTime[DateTime])
                    (from: DateTime): Option[DateTime] = step(expr, dt)(from, 1)

  @inline
  def prev[DateTime](expr: T, dt: IsDateTime[DateTime])
                    (from: DateTime): Option[DateTime] = step(expr, dt)(from, -1)

  def step[DateTime](expr: T, dt: IsDateTime[DateTime])
                    (from: DateTime, stepSize: Int): Option[DateTime]

  def ranges(expr: T): Map[CronField, IndexedSeq[Int]]

  def supportedFields: List[CronField]

  def field[F <: CronField](expr: T)(implicit selector: FieldSelector[T, F]): selector.Out[F] =
    selector.selectFrom(expr)

}

object DateTimeCron {
  @inline def apply[T](implicit ev: DateTimeCron[T]): DateTimeCron[T] = ev

  implicit val fullCronInstance: DateTimeCron[CronExpr]     = new FullCron
  implicit val timeCronInstance: DateTimeCron[TimeCronExpr] = new TimeCron
  implicit val dateCronInstance: DateTimeCron[DateCronExpr] = new DateCron
}

private[datetime] final class FullCron extends DateTimeCron[CronExpr] {

  protected def matches[DateTime](expr: CronExpr, dt: IsDateTime[DateTime])
      (implicit M: PlusEmpty[Predicate]): Predicate[DateTime] = {
    val reducer = new PredicateReducer[DateTime](dt)
    reducer.run(Coproduct[AnyCron](expr))
  }

  def step[DateTime](expr: CronExpr, dt: IsDateTime[DateTime])
      (from: DateTime, amount: Int): Option[DateTime] = {
    val stepper = new Stepper[DateTime](dt)
    val direction = Direction.of(amount)
    for {
      (adjustedTime, carryOver, _) <- stepper.stepOverTime(expr.timePart.raw, from, amount, direction)
      (adjustedDate, _, _)         <- stepper.stepOverDate(expr.datePart.raw, adjustedTime, carryOver, direction)(allOf(expr, dt))
    } yield adjustedDate
  }

  def ranges(expr: CronExpr): Map[CronField, IndexedSeq[Int]] =
    supportedFields.zip(expr.raw.map(ops.range).toList).toMap

  @inline
  val supportedFields: List[CronField] = CronField.All

}

private[datetime] final class TimeCron extends DateTimeCron[TimeCronExpr] {

  protected def matches[DateTime](expr: TimeCronExpr, dt: IsDateTime[DateTime])
      (implicit M: PlusEmpty[Predicate]): Predicate[DateTime] = {
    val reducer = new PredicateReducer[DateTime](dt)
    reducer.run(Coproduct[AnyCron](expr))
  }

  def step[DateTime](expr: TimeCronExpr, dt: IsDateTime[DateTime])
      (from: DateTime, stepSize: Int): Option[DateTime] = {
    val stepper = new Stepper[DateTime](dt)
    stepper.stepOverTime(expr.raw, from, stepSize, Direction.of(stepSize)).map(_._1)
  }

  def ranges(expr: TimeCronExpr): Map[CronField, IndexedSeq[Int]] =
    supportedFields.zip(expr.raw.map(ops.range).toList).toMap

  @inline
  val supportedFields: List[CronField] =
    List(CronField.Second, CronField.Minute, CronField.Hour)

}

private[datetime] final class DateCron extends DateTimeCron[DateCronExpr] {

  protected def matches[DateTime](expr: DateCronExpr, dt: IsDateTime[DateTime])
      (implicit M: PlusEmpty[Predicate]): Predicate[DateTime] = {
    val reducer = new PredicateReducer[DateTime](dt)
    reducer.run(Coproduct[AnyCron](expr))
  }

  def step[DateTime](expr: DateCronExpr, dt: IsDateTime[DateTime])
      (from: DateTime, stepSize: Int): Option[DateTime] = {
    val stepper = new Stepper[DateTime](dt)
    stepper.stepOverDate(expr.raw, from, stepSize, Direction.of(stepSize))(allOf(expr, dt)).map(_._1)
  }

  def ranges(expr: DateCronExpr): Map[CronField, IndexedSeq[Int]] =
    supportedFields.zip(expr.raw.map(ops.range).toList).toMap

  @inline
  val supportedFields: List[CronField] =
    List(CronField.DayOfMonth, CronField.Month, CronField.DayOfWeek)

}
