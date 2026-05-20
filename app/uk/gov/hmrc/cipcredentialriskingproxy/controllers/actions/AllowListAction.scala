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

import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.mvc.*
import play.api.mvc.Results.Forbidden
import uk.gov.hmrc.cipcredentialriskingproxy.config.{AppConfig, Constants}
import uk.gov.hmrc.cipcredentialriskingproxy.models.{AccessForbidden, Error}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait AllowListAction {
  def apply(): ActionBuilder[Request, AnyContent]
}

class AllowListActionImpl @Inject()(config: AppConfig,
                                    parsers: PlayBodyParsers)(implicit ec: ExecutionContext) extends AllowListAction:

  override def apply(): ActionBuilder[Request, AnyContent] =
    new ActionBuilder[Request, AnyContent]:

      override def parser: BodyParser[AnyContent] = parsers.default
      override def executionContext: ExecutionContext = ec

      override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =

        val callingClients = request.headers.get("OriginatorId").fold(
          request.headers.getAll(HeaderNames.USER_AGENT).flatMap(_.split(","))
        )(Seq(_))

        if (!config.allowListEnabled || callingClients.forall(config.allowedClients.contains))
          block(request)
        else
          Future.successful(
            Forbidden(Json.toJson[Error](AccessForbidden(callingClients, config.accessRequestFormUrl)))
              .withHeaders(Seq(
                request.headers.get(Constants.xCorrelationId).map(Constants.xCorrelationId -> _)
              ).flatten:_*)
          )

