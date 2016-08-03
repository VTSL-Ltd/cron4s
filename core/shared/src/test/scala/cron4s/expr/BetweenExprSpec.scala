package cron4s.expr

import cron4s.types.Sequential
import org.scalacheck._

/**
  * Created by alonsodomin on 31/07/2016.
  */
object BetweenExprSpec extends Properties("BetweenExpr") with ExprGenerators {
  import Prop._
  import Arbitrary.arbitrary

  property("min should be the begin constant") = forAll(betweenExpressions) {
    expr => expr.begin.value == expr.min
  }

  property("max should be the end constant") = forAll(betweenExpressions) {
    expr => expr.end.value == expr.max
  }

  property("range must be from the begining to the end inclusive") = forAll(betweenExpressions) {
    expr => expr.range == (expr.begin.value to expr.end.value)
  }

  val valuesOutsideRange = for {
    expr  <- betweenExpressions
    value <- arbitrary[Int] if value < expr.min || value > expr.max
  } yield (expr, value)

  val valuesInsideRange = for {
    expr  <- betweenExpressions
    value <- Gen.choose(expr.min, expr.max)
  } yield (expr, value)

  property("should not match values outside its own range") = forAll(valuesOutsideRange) {
    case (expr, value) => !expr.matches(value)
  }

  property("should match values inside its own range") = forAll(valuesInsideRange) {
    case (expr, value) => expr.matches(value)
  }

  val stepsFromOutsideRange = for {
    expr      <- betweenExpressions
    fromValue <- arbitrary[Int] if fromValue < expr.unit.min || fromValue > expr.unit.max
    stepSize  <- arbitrary[Int]
  } yield (expr, fromValue, stepSize)

  property("stepping from outside the range returns none") = forAll(stepsFromOutsideRange) {
    case (expr, fromValue, stepSize) => expr.step(fromValue, stepSize).isEmpty
  }

  val stepsFromInsideRange = for {
    expr      <- betweenExpressions
    fromValue <- Gen.choose(expr.min, expr.max)
    stepSize  <- arbitrary[Int]
  } yield (expr, fromValue, stepSize)

  property("stepping with a zero step size returns the from value") = forAll(stepsFromInsideRange) {
    case (expr, fromValue, _) => expr.step(fromValue, 0).contains((fromValue, 0))
  }

  val stepsIntoInsideRange = for {
    expr      <- betweenExpressions
    fromValue <- Gen.choose(expr.unit.min, expr.unit.max)
    stepSize  <- arbitrary[Int]
  } yield (expr, fromValue, stepSize)

  property("stepping from inside the range is the same as narrowing the unit") = forAll(stepsIntoInsideRange) {
    case (expr, fromValue, stepSize) =>
      val rangeStep = Sequential.sequential(expr.range).step(fromValue, stepSize)
      expr.step(fromValue, stepSize) == rangeStep
  }

}