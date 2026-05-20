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

package uk.gov.hmrc.cipcredentialriskingproxy

import play.api.inject.{Binding, Module as AppModule}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.cipcredentialriskingproxy.config.{AppConfig, AppConfigImpl}
import uk.gov.hmrc.cipcredentialriskingproxy.controllers.actions.{AllowListAction, AllowListActionImpl, CorrelationIdAction, CorrelationIdActionImpl}

import java.time.Clock

class Module extends AppModule:

  override def bindings(environment  : Environment, configuration: Configuration): Seq[Binding[_]] =
    Seq(
      bind[Clock].toInstance(Clock.systemDefaultZone),
      bind[AppConfig].to[AppConfigImpl].eagerly(),
      bind[AllowListAction].to[AllowListActionImpl].eagerly(),
      bind[CorrelationIdAction].to[CorrelationIdActionImpl].eagerly()
    )

