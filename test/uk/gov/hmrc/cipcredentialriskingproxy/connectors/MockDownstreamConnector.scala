/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.cipcredentialriskingproxy.connectors

import izumi.reflect.Tag
import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.libs.ws.BodyWritable
import play.api.mvc.{Request, Result}

import scala.concurrent.{ExecutionContext, Future}

trait MockDownstreamConnector extends MockFactory:
  this: TestSuite =>

    lazy val mockDownstreamConnector: DownstreamConnector = mock[DownstreamConnector]

    object MockDownstreamConnector:
      def forward[A](): CallHandler4[Request[A], ExecutionContext, BodyWritable[A], Tag[A], Future[Result]] =
        (mockDownstreamConnector.forward[A](_: Request[A], _: ExecutionContext, _: BodyWritable[A], _: Tag[A]))
          .expects(*, *, *, *)
          .once()
