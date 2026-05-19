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

package uk.gov.hmrc.cipcredentialriskingproxy.testOnly.controllers

import izumi.reflect.Tag
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.libs.ws.{BodyWritable, writeableOf_JsValue}
import play.api.mvc.*
import uk.gov.hmrc.cipcredentialriskingproxy.utils.CustomBodyWritables.writeableOf_Unit
import uk.gov.hmrc.cipcredentialriskingproxy.utils.ProxyRequestHelper
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.annotation.unused
import scala.concurrent.{ExecutionContext, Future}

class TestOnlyProxyController @Inject()(cc: ControllerComponents,
                                        servicesConfig: ServicesConfig,
                                        override val httpClient: HttpClientV2)(implicit ec: ExecutionContext) extends BackendController(cc) with Logging with ProxyRequestHelper:

  def proxyWithJsonBody(@unused path: String): Action[JsValue] = Action.async(parse.json):
    implicit request =>
      proxyRequest[JsValue]

  def proxyNoBody(@unused path: String): Action[Unit] = Action.async(parse.empty):
    implicit request =>
      proxyRequest[Unit]


  private def proxyRequest[A](implicit request: Request[A], writes: BodyWritable[A], tag: Tag[A]): Future[Result] =
    streamProxyResponse(buildProxyRequest(
      request = request,
      host = servicesConfig.baseUrl("cip-credential-risking"),
      path = request.target.uriString.replace("cip-credential-risking/", "")
    ))