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

package uk.gov.hmrc.cipcredentialriskingproxy.controllers.actions

import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.Results.BadRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.cipcredentialriskingproxy.BaseSpec
import uk.gov.hmrc.cipcredentialriskingproxy.config.{Constants, MockAppConfig}
import uk.gov.hmrc.cipcredentialriskingproxy.models.{Error, MissingCorrelationId, RequestWithCorrelationId}

import scala.concurrent.ExecutionContext

class CorrelationIdActionSpec extends BaseSpec with MockAppConfig {

  private val action = new CorrelationIdActionImpl()(ExecutionContext.global)

  val testCorrelationId = "some-correlation-from-upstream"
  val userId  = "1234567890"
  val sessionId = "0987654321"

  def fakeRequest(correlationId: Option[String]): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", s"/$userId/score/$sessionId")
      .withHeaders(Seq(correlationId.map(id => Constants.xCorrelationId -> id)).flatten: _*)

  "CorrelationIdAction" should {

    "add correlationId to the request when provided" in {
      val request = fakeRequest(Some(testCorrelationId))
      await(action.refine(request)) shouldBe
        Right(RequestWithCorrelationId(testCorrelationId, request))
    }

    "return BadRequest when correlationId is missing" in {
      await(action.refine(fakeRequest(correlationId = None))) shouldBe
        Left(BadRequest(Json.toJson[Error](MissingCorrelationId)))
    }

    "return BadRequest when correlationId exists but is blank" in {
      await(action.refine(fakeRequest(correlationId = Some("    ")))) shouldBe
        Left(BadRequest(Json.toJson[Error](MissingCorrelationId)))
    }
  }
}
