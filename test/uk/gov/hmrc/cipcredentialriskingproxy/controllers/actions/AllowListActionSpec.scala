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

import org.scalatest.wordspec.AnyWordSpec
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.cipcredentialriskingproxy.BaseSpec
import uk.gov.hmrc.cipcredentialriskingproxy.config.{Constants, MockAppConfig}
import uk.gov.hmrc.cipcredentialriskingproxy.models.{AccessForbidden, Error}

class AllowListActionSpec extends BaseSpec with MockAppConfig:

  val action = new AllowListActionImpl(mockAppConfig, Helpers.stubPlayBodyParsers())

  private val userId  = "1234567890"
  private val sessionId = "0987654321"

  ".checkAllowList()" when:

    val baseRequest =
      FakeRequest("GET", s"/$userId/score/$sessionId")

    "the allow list is enabled" when:

      "the User-Agent is on the allow list" should:

        "return a OK (200)" in:

          MockAppConfig.allowListEnabled(true)
          MockAppConfig.allowedClients(Set("foo-agent", "bar-agent"))

          val fakeRequest = baseRequest
            .withHeaders(
              HeaderNames.USER_AGENT -> "foo-agent"
            )

          val result = action()(Results.Ok("allowed"))(fakeRequest)

          status(result) shouldBe Status.OK
          contentAsString(result) shouldBe "allowed"

      "the User-Agent is NOT on the allow list" should :

        "return a Forbidden (403)" in :

          MockAppConfig.allowListEnabled(true)
          MockAppConfig.allowedClients(Set("foo-agent", "bar-agent"))
          MockAppConfig.accessRequestFormUrl("/request-access")

          val fakeRequest = baseRequest
            .withHeaders(
              Constants.correlationId -> "some-correlation-id-from-upstream",
              HeaderNames.USER_AGENT -> "other-agent"
            )

          val result = action()(Results.Ok("allowed"))(fakeRequest)

          status(result) shouldBe Status.FORBIDDEN
          contentAsJson(result) shouldBe Json.toJson[Error](AccessForbidden(
            callingClients = Seq("other-agent"),
            formUrl = "/request-access"
          ))
          headers(result) should contain(Constants.correlationId -> "some-correlation-id-from-upstream")

      "the OriginatorId is set and is on the allow list" should :

        "return a OK (200)" in :

          MockAppConfig.allowListEnabled(true)
          MockAppConfig.allowedClients(Set("foo-agent", "bar-agent"))

          val fakeRequest = baseRequest
            .withHeaders(
              "OriginatorId" -> "bar-agent",
              HeaderNames.USER_AGENT -> "other-agent",
              HeaderNames.CONTENT_TYPE -> MimeTypes.JSON
            )

          val result = action()(Results.Ok("allowed"))(fakeRequest)

          status(result) shouldBe Status.OK
          contentAsString(result) shouldBe "allowed"

      "the OriginatorId is set and is NOT on the allow list" should :

        "return a Forbidden (403)" in :

          MockAppConfig.allowListEnabled(true)
          MockAppConfig.allowedClients(Set("foo-agent", "bar-agent"))
          MockAppConfig.accessRequestFormUrl("/request-access")

          val fakeRequest = baseRequest
            .withHeaders(
              "OriginatorId" -> "other-agent",
              Constants.correlationId -> "some-correlation-id-from-upstream",
              HeaderNames.USER_AGENT -> "other-agent",
              HeaderNames.CONTENT_TYPE -> MimeTypes.JSON
            )

          val result = action()(Results.Ok("allowed"))(fakeRequest)

          status(result) shouldBe Status.FORBIDDEN
          contentAsJson(result) shouldBe Json.toJson[Error](AccessForbidden(
            callingClients = Seq("other-agent"),
            formUrl = "/request-access"
          ))
          headers(result) should contain(Constants.correlationId -> "some-correlation-id-from-upstream")