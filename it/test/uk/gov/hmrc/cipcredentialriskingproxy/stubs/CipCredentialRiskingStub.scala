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

package uk.gov.hmrc.cipcredentialriskingproxy.stubs

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.cipcredentialriskingproxy.BaseISpec
import uk.gov.hmrc.cipcredentialriskingproxy.config.Constants

trait CipCredentialRiskingStub extends BaseStubMapping:
  this: BaseISpec =>

  object CipCredentialRisking:

    private def baseCredentialRiskingCheck(method: String, path: String): MappingBuilder =
      baseMapping(method, path)
        .withHeader(Constants.xCorrelationId, equalTo("some-correlation-id-from-upstream"))

    def success(method: String, path: String)(status: Int, body: JsValue): StubMapping =
      wireMockServer.stubFor(
        baseCredentialRiskingCheck(method, path).willReturn(
          aResponse()
            .withStatus(status)
            .withHeader(Constants.xCorrelationId, "some-correlation-id-from-upstream")
            .withBody(Json.stringify(body))
        )
      )

    def failure(method: String, path: String): StubMapping =
      wireMockServer.stubFor(
        baseCredentialRiskingCheck(method, path).willReturn(
          aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)
        )
      )
