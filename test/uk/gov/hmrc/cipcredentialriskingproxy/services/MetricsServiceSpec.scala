/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.cipcredentialriskingproxy.services

import com.codahale.metrics.MetricRegistry
import uk.gov.hmrc.cipcredentialriskingproxy.BaseSpec

import scala.concurrent.Future

class MetricsServiceSpec extends BaseSpec :

  val metricRegistry = new MetricRegistry()
  val service        = new MetricsService(metricRegistry)

  "withTimer" should :

    "create a timer, start it and stop it on success" in :
      service
        .withTimer("test-timer") {
          Thread.sleep(500)
          Future.successful(42)
        }
        .futureValue shouldBe 42

      val timer = metricRegistry.getTimers.get("test-timer")

      timer                    should not be null
      timer.getCount           shouldBe 1
      timer.getSnapshot.getMax should be >= 500000000L // 500ms in nanoseconds

    "create a timer, start it and stop it on failure" in :
      intercept[RuntimeException](
        service
          .withTimer("test-timer-failed") {
            Thread.sleep(500)
            Future.failed[Int](new RuntimeException("fail"))
          }
          .futureValue
      )

      val timer = metricRegistry.getTimers.get("test-timer-failed")

      timer                    should not be null
      timer.getCount           shouldBe 1
      timer.getSnapshot.getMax should be >= 500000000L // 500ms in nanoseconds
