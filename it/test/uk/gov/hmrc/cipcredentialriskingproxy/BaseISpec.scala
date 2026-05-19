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

package uk.gov.hmrc.cipcredentialriskingproxy

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import uk.gov.hmrc.cipcredentialriskingproxy.config.AppConfig
import uk.gov.hmrc.http.test.WireMockSupport

import scala.concurrent.ExecutionContext

trait BaseISpec extends AnyWordSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with GuiceOneServerPerSuite
  with WireMockSupport:

  implicit val mat: Materializer = Materializer(ActorSystem("ispecs"))
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val baseUrl: String = s"http://localhost:$port"
  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes")
      .configure("auditing.enabled" -> false)
      .configure("microservice.services.cip-credential-risking.port" -> wireMockPort)
      .build()