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

package uk.gov.hmrc.cipcredentialriskingproxy.controllers

import org.scalatest.OneInstancePerTest
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.cipcredentialriskingproxy.BaseSpec
import uk.gov.hmrc.cipcredentialriskingproxy.config.Constants
import uk.gov.hmrc.cipcredentialriskingproxy.connectors.MockDownstreamConnector
import uk.gov.hmrc.cipcredentialriskingproxy.controllers.actions.{CorrelationIdActionImpl, FakeAllowListAction}
import uk.gov.hmrc.cipcredentialriskingproxy.models.{Error, FailedFuture}

import scala.concurrent.Future

class ProxyControllerSpec extends BaseSpec with MockDownstreamConnector with OneInstancePerTest:

  private def controller(userIsAllowed: Boolean): ProxyController =
    new ProxyController(
      cc = Helpers.stubControllerComponents(),
      connector = mockDownstreamConnector,
      allowListAction = new FakeAllowListAction(Helpers.stubPlayBodyParsers())(isAllowed = userIsAllowed),
      withCorrelationId = new CorrelationIdActionImpl()
    )

  private def authorisedController: ProxyController = controller(userIsAllowed = true)
  private def unauthorisedController: ProxyController = controller(userIsAllowed = false)

  private val userId    = "1234567890"
  private val sessionId = "0987654321"

  "GET /:userId/score/:sessionId" when:

    def fakeRequest: FakeRequest[Unit] = FakeRequest("GET", s"/$userId/score/$sessionId")
      .withBody(())
      .withHeaders(Constants.correlationId -> "some-correlation-id-from-upstream")

    "client is NOT authorised" when:

      "return a 403 Forbidden" in:
        val result = await(unauthorisedController.proxyNoBody(fakeRequest.path)(fakeRequest))
        result.header.status shouldBe Status.FORBIDDEN

    "client is authorised" when:

      "the downstream returns a response" should:

        "return the provided response and body from the downstream (On Success 2xx)" in:

          MockDownstreamConnector.forward[Unit]().returns(Future.successful(Results.Ok("Success!")))

          val result = await(authorisedController.proxyNoBody(fakeRequest.path)(fakeRequest))

          result.header.status shouldBe Status.OK
          contentAsString(Future.successful(result)) shouldBe "Success!"

        "return the provided response and body from the downstream (On Error !2xx)" in:

          MockDownstreamConnector.forward[Unit]().returns(Future.successful(Results.BadRequest("Invalid Payload!")))

          val result = await(authorisedController.proxyNoBody(fakeRequest.path)(fakeRequest))

          result.header.status shouldBe Status.BAD_REQUEST
          contentAsString(Future.successful(result)) shouldBe "Invalid Payload!"

      "the downstream returns an unexpected failed future" should:

        "return an ISE with the Failed Future error response" in:

          MockDownstreamConnector.forward[Unit]().returns(Future.failed(new Exception("bang!")))

          val result = await(authorisedController.proxyNoBody(fakeRequest.path)(fakeRequest))

          result.header.status shouldBe Status.INTERNAL_SERVER_ERROR
          contentAsJson(Future.successful(result)) shouldBe Json.toJson[Error](FailedFuture)
