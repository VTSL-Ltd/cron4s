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

package cron4s.testkit.gen

import cron4s.CronField._

import org.scalacheck.Arbitrary

/**
  * Created by alonsodomin on 28/08/2016.
  */
trait ArbitraryBetweenNode extends NodeGenerators {

  implicit lazy val arbitraryBetweenSecond = Arbitrary(betweenGen[Second])
  implicit lazy val arbitraryBetweenMinute = Arbitrary(betweenGen[Minute])
  implicit lazy val arbitraryBetweenHour = Arbitrary(betweenGen[Hour])
  implicit lazy val arbitraryBetweenDayOfMonth = Arbitrary(betweenGen[DayOfMonth])
  implicit lazy val arbitraryBetweenMonth = Arbitrary(betweenGen[Month])
  implicit lazy val arbitraryBetweenDayOfWeek = Arbitrary(betweenGen[DayOfWeek])

}