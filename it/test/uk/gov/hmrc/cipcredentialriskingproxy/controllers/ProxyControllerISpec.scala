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

package uk.gov.hmrc.cipcredentialriskingproxy.controllers

import org.scalatest.wordspec.AnyWordSpec
import play.api.http.{MimeTypes, Status}
import play.api.libs.json.Json
import play.api.test.Helpers.GET
import sttp.model.HeaderNames
import uk.gov.hmrc.cipcredentialriskingproxy.BaseISpec
import uk.gov.hmrc.cipcredentialriskingproxy.config.Constants
import uk.gov.hmrc.cipcredentialriskingproxy.models.{AccessForbidden, DownstreamError, Error}
import uk.gov.hmrc.cipcredentialriskingproxy.stubs.CipCredentialRiskingStub

class ProxyControllerISpec extends BaseISpec with CipCredentialRiskingStub:

  val userId    = "1234567890"
  val sessionId = "0987654321"

  "GET /:userId/score/:sessionId" when:

    "the client is NOT authorised" should:

      "respond with FORBIDDEN (403) status" in:

        val result =
          wsClient
            .url(s"$baseUrl/credential-risking-proxy/$userId/score/$sessionId")
            .withHttpHeaders(
              HeaderNames.UserAgent -> "unauthorised-agent",
              HeaderNames.ContentType -> MimeTypes.JSON,
              Constants.correlationId -> "some-correlation-id-from-upstream"
            )
            .get()
            .futureValue

        result.status shouldBe Status.FORBIDDEN
        result.json shouldBe Json.toJson[Error](AccessForbidden(
          callingClients = Seq("unauthorised-agent"),
          formUrl = appConfig.accessRequestFormUrl
        ))

    "the client is authorised to call this endpoint" when:

      lazy val baseRequest =
        wsClient
          .url(s"$baseUrl/credential-risking-proxy/$userId/score/$sessionId")
          .withHttpHeaders(
            HeaderNames.UserAgent -> "test-only",
            HeaderNames.ContentType -> MimeTypes.JSON,
            Constants.correlationId -> "some-correlation-id-from-upstream"
          )

      "downstream connector is successful" when:

          "respond with OK (200) status" in:

            CipCredentialRisking.success(GET, s"/credential-risking/$userId/score/$sessionId")(
              Status.OK, Json.obj("score" -> 42)
            )

            val result =
              baseRequest
                .get()
                .futureValue

            result.status shouldBe Status.OK
            result.json shouldBe Json.obj("score" -> 42)
            result.header(Constants.correlationId) shouldBe Some("some-correlation-id-from-upstream")

      "downstream connector fails" when:

          "respond with BAD_GATEWAY (502) status" in:

            CipCredentialRisking.failure(GET, s"/credential-risking/$userId/score/$sessionId")

            val result =
              baseRequest
                .get()
                .futureValue

            result.status shouldBe Status.BAD_GATEWAY
            result.json shouldBe Json.toJson[Error](DownstreamError)
            result.header(Constants.correlationId) shouldBe Some("some-correlation-id-from-upstream")





