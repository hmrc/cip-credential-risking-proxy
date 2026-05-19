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

import izumi.reflect.Tag
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.BodyWritable
import play.api.mvc.{Action, ControllerComponents, Result}
import uk.gov.hmrc.cipcredentialriskingproxy.config.Constants
import uk.gov.hmrc.cipcredentialriskingproxy.connectors.DownstreamConnector
import uk.gov.hmrc.cipcredentialriskingproxy.controllers.actions.{AllowListAction, CorrelationIdAction}
import uk.gov.hmrc.cipcredentialriskingproxy.models.{Error, FailedFuture, RequestWithCorrelationId}
import uk.gov.hmrc.cipcredentialriskingproxy.utils.CustomBodyWritables.writeableOf_Unit
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.annotation.unused
import scala.concurrent.{ExecutionContext, Future}


class ProxyController @Inject()(cc: ControllerComponents,
                                connector: DownstreamConnector,
                                allowListAction: AllowListAction,
                                withCorrelationId: CorrelationIdAction)(implicit ec: ExecutionContext) extends BackendController(cc) with Logging:

  private val authAction = allowListAction() andThen withCorrelationId

  def proxyNoBody(@unused path: String): Action[Unit] = authAction.async(parse.empty):
    implicit request =>
      proxyRequest[Unit]

  private def proxyRequest[A](implicit request: RequestWithCorrelationId[A], writes: BodyWritable[A], tag: Tag[A]): Future[Result] =
    (connector.forward recover {
      case t: Throwable =>
        logger.error(s"[proxyRequest] An exception of type '${t.getClass.getSimpleName}' occurred when trying to forward the request")
        InternalServerError(Json.toJson[Error](FailedFuture))
    }).map(_.withHeaders(Constants.xCorrelationId -> request.correlationId))
