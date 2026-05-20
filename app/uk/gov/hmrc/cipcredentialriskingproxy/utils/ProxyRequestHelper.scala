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

package uk.gov.hmrc.cipcredentialriskingproxy.utils

import izumi.reflect.Tag
import play.api.Logging
import play.api.http.HeaderNames.*
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.libs.ws.BodyWritable
import play.api.mvc.Results.{BadGateway, MethodNotAllowed}
import play.api.mvc.{Request, ResponseHeader, Result}
import uk.gov.hmrc.cipcredentialriskingproxy.models.{DownstreamError, Error}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder, readStreamHttpResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

trait ProxyRequestHelper extends Logging {

  val httpClient: HttpClientV2

  def buildProxyRequest[A](
                            request: Request[A],
                            host: String,
                            path: String
                          )(implicit hc: HeaderCarrier, ec: ExecutionContext, writes: BodyWritable[A], tag: Tag[A]): Either[Error, RequestBuilder] = {
    val uri = url"${host + path}"
    (request.method match {
      case "GET" => Right(httpClient.get(uri))
      case "POST" => Right(httpClient.post(uri).withBody(request.body))
      case "DELETE" => Right(httpClient.delete(uri))
      case _ => Left(DownstreamError)
    }).map(_.setHeader(buildOnwardHeaders(request): _*))
  }

  def streamProxyResponse(proxyRequest: => Either[Error, RequestBuilder])(implicit ec: ExecutionContext): Future[Result] =
    proxyRequest.fold(
      err => Future.successful(MethodNotAllowed(Json.toJson[Error](err))),
      _.stream[HttpResponse]
        .map { (response: HttpResponse) =>
          Result(
            ResponseHeader(response.status, cleanseResponseHeaders(response)),
            HttpEntity.Streamed(response.bodyAsSource, None, response.header(CONTENT_TYPE))
          )
        }.recover { case t: Throwable =>
          logger.warn(s"[streamProxyResponse] An exception of type '${t.getClass.getSimpleName}' occurred when the downstream service tried to handle the request")
          BadGateway(Json.toJson[Error](DownstreamError))
        }
    )

  private def buildOnwardHeaders[A]: Request[A] => Seq[(String, String)] =
    _.headers.remove(CONTENT_LENGTH, HOST, AUTHORIZATION).headers

  private def cleanseResponseHeaders(response: HttpResponse): Map[String, String] =
    response.headers
      .filterNot { case (k, _) =>
        Seq(CONTENT_TYPE, CONTENT_LENGTH, TRANSFER_ENCODING).map(_.toUpperCase).contains(k.toUpperCase)
      }
      .view.mapValues(_.mkString).toMap
}
