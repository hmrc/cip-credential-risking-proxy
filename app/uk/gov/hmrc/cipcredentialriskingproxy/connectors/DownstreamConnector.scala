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
import play.api.Logging
import play.api.libs.ws.BodyWritable
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.cipcredentialriskingproxy.config.AppConfig
import uk.gov.hmrc.cipcredentialriskingproxy.services.MetricsService
import uk.gov.hmrc.cipcredentialriskingproxy.utils.ProxyRequestHelper
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class DownstreamConnector @Inject()(override val httpClient: HttpClientV2,
                                    config: AppConfig, metricService: MetricsService) extends Logging with ProxyRequestHelper:

  def forward[A](implicit request: Request[A], ec: ExecutionContext, writes: BodyWritable[A], tag: Tag[A]): Future[Result] =

    implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = Seq())

    metricService.withTimer("DownstreamConnector.forward") {
      streamProxyResponse(buildProxyRequest[A](
        request = request,
        host = config.cipCredentialRiskingScoreBaseUrl,
        path = request.target.uriString.replace("credential-risking-proxy", "credential-risking")
      ))
    }
