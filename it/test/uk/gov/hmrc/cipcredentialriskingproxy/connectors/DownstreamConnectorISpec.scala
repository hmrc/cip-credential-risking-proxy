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

import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.writeableOf_JsValue
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.cipcredentialriskingproxy.BaseISpec
import uk.gov.hmrc.cipcredentialriskingproxy.config.Constants
import uk.gov.hmrc.cipcredentialriskingproxy.models.{DownstreamError, Error}
import uk.gov.hmrc.cipcredentialriskingproxy.stubs.CipCredentialRiskingStub

class DownstreamConnectorISpec extends BaseISpec with CipCredentialRiskingStub:

  lazy val connector: DownstreamConnector = app.injector.instanceOf[DownstreamConnector]

  ".forward()" when:

    def requestFor(method: String): Request[JsValue] =
      FakeRequest(method, "/cip-credential-risking/get/some/risking/score")
        .withHeaders(Constants.correlationId -> "some-correlation-id-from-upstream")
        .withBody(Json.obj())

    Seq(POST, GET, DELETE).foreach { method =>
      s"downstream connector is successful for a $method" should :

        "respond with OK (200) status" in :

          CipCredentialRisking.success(method, "/cip-credential-risking/get/some/risking/score")(Status.OK, Json.obj("score" -> 42))

          val result = connector.forward(requestFor(method))

          status(result) shouldBe Status.OK
          contentAsJson(result) shouldBe Json.obj("score" -> 42)
          headers(result) should contain(Constants.correlationId -> "some-correlation-id-from-upstream")

      s"downstream connector fails for a $method" should :

        "respond with BAD_GATEWAY (502) status" in :

          CipCredentialRisking.failure(method, "/cip-credential-risking/get/some/risking/score")

          val result = connector.forward(requestFor(method))

          status(result) shouldBe Status.BAD_GATEWAY
          contentAsJson(result) shouldBe Json.toJson[Error](DownstreamError)
    }

    "downstream connector responds with METHOD_NOT_ALLOWED for unsupported HTTP methods" should :

      "return METHOD_NOT_ALLOWED (405) status" in :
        val unsupportedMethodRequest = FakeRequest("PATCH", "/cip-credential-risking/get/some/risking/score")
        .withHeaders(Constants.correlationId -> "some-correlation-id-from-upstream")
        .withBody(Json.obj())

        val result = connector.forward(unsupportedMethodRequest)

        status(result) shouldBe Status.METHOD_NOT_ALLOWED
        contentAsJson(result) shouldBe Json.toJson[Error](DownstreamError)


