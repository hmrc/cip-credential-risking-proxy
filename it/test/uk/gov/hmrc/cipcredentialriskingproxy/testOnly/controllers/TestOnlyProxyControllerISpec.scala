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

/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.cipcredentialriskingproxy.testOnly.controllers

import org.scalatest.wordspec.AnyWordSpec
import play.api.http.{MimeTypes, Status}
import play.api.libs.json.Json
import play.api.libs.ws.DefaultBodyWritables.writeableOf_String
import play.api.test.Helpers.{DELETE, POST}
import sttp.model.HeaderNames
import uk.gov.hmrc.cipcredentialriskingproxy.BaseISpec
import uk.gov.hmrc.cipcredentialriskingproxy.stubs.CipCredentialRiskingTestOnlyStub

class TestOnlyProxyControllerISpec extends BaseISpec with CipCredentialRiskingTestOnlyStub:

  "For the test only endpoints on cip-credential-risking" when:

    "POST /test-only/cip-credential-risking/test-data" when :

      lazy val baseRequest = wsClient
        .url(s"$baseUrl/test-only/cip-credential-risking/test-data")
        .withHttpHeaders(HeaderNames.ContentType -> MimeTypes.JSON)

      "valid JSON payload is received" when :

        "proxy is successful" should :

          "respond with OK (200) status" in :

            CipCredentialRiskingTestOnly.success(POST, "/test-only/test-data")(
              Status.OK, Some(Json.arr("userIds" -> "1703014526153725"))
            )

            val result =
              baseRequest
                .post(Json.stringify(Json.obj("knownUserIdCount" -> "10", "alertCount" -> "5")))
                .futureValue

            result.status shouldBe Status.OK
            result.json shouldBe Json.arr("userIds" -> "1703014526153725")

      "invalid JSON payload is received" should :

        "return BAD_REQUEST (400) status" in :

          val result = baseRequest.post("This is not JSON!").futureValue

          result.status shouldBe Status.BAD_REQUEST
          (result.json \ "statusCode").as[Int] shouldBe Status.BAD_REQUEST
          (result.json \ "message").as[String] should include("Invalid Json")


    "DELETE /test-only/cip-risk/str/vertex-data" when :

      lazy val baseRequest = wsClient.url(s"$baseUrl/test-only/cip-credential-risking/test-data")

      "proxy is successful" when :

        "respond with OK (204) status" in :

          CipCredentialRiskingTestOnly.success(DELETE, "/test-only/test-data")(
            Status.NO_CONTENT, None
          )

          val result = baseRequest.delete().futureValue

          result.status shouldBe Status.NO_CONTENT
