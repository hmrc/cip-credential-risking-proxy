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

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MetricsService @Inject()(metricsRegistry: MetricRegistry) :

  def withTimer[T](timerName: String)(f: => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val timer = metricsRegistry.timer(timerName).time()
    f andThen { case _ =>
      timer.stop()
    }
  }
