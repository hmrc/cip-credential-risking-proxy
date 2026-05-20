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

trait BaseStubMapping:

  def baseMapping(method: String, path: String): MappingBuilder =
    method.toUpperCase match
      case "POST" => post(urlEqualTo(path))
      case "GET" => get(urlEqualTo(path))
      case "DELETE" => delete(urlEqualTo(path))
      case _ => throw new IllegalArgumentException(s"Unsupported method: $method")