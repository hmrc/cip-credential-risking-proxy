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

package uk.gov.hmrc.cipcredentialriskingproxy.models

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.cipcredentialriskingproxy.config.Constants

sealed class Error(val code: String, val desc: String)

case object FailedFuture extends
  Error("FAILED_FUTURE", "An unrecoverable failed future occurred when forwarding the request to the downstream service")

case object DownstreamError extends
  Error("REQUEST_DOWNSTREAM", "An unrecoverable error occurred when the downstream service tried to handle the request")

case object MissingCorrelationId extends
  Error("MISSING_CORRELATION_ID", s"${Constants.correlationId} header is missing from the request")

case class AccessForbidden(callingClients: Seq[String], formUrl: String) extends
  Error(
    "USER_NOT_ALLOWED",
    s"One or more user agents in '${callingClients.mkString(",")}' are not authorized to use this service. Please complete '$formUrl' to request access."
  )

object Error {
  implicit val writes: Writes[Error] = Writes { model =>
    Json.obj(
      "statusCode" -> model.code,
      "message" -> model.desc
    )
  }
}
