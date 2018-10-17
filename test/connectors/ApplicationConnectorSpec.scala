/*
 * Copyright 2018 HM Revenue & Customs
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

package connectors

import config.WSHttp
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.WireMockHelper

import com.github.tomakehurst.wiremock.client.WireMock._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ApplicationConnectorSpec extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures with IntegrationPatience with GuiceOneAppPerSuite with WireMockHelper {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  override lazy val app: Application = new GuiceApplicationBuilder().configure("connector.port" → server.port()).build()
  lazy val connector: ApplicationConnector = app.injector.instanceOf[ApplicationConnector]

  ".getResponse" should {
    "return full response" when {
      "response status is OK" in {
        server.stubFor(get(urlPathEqualTo(connector.url))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody("""{"response": true}""")))

        val response: HttpResponse = HttpResponse(200, None, Map(),Some("""{"response": true}"""))

        whenReady(connector.getResponse) {
          resp ⇒
            resp.status mustBe 200
            resp.body mustBe response.body
        }

      }
    }

    "throw HttpException" when {
      "response status is 204" in {
        server.stubFor(get(urlPathEqualTo(connector.url))
          .willReturn(
            aResponse()
              .withStatus(204)))

        intercept[ApplicationHttpException]{
          Await.result(connector.getResponse, Duration.Inf)
        }
      }

      "response status is 404" in {
        server.stubFor(get(urlPathEqualTo(connector.url))
          .willReturn(
            aResponse()
              .withStatus(404)))

        Await.result(connector.getResponse, Duration.Inf) mustBe connector.defaultNotFoundResponse
      }

      "response status is 500" in {
        server.stubFor(get(urlPathEqualTo(connector.url))
          .willReturn(
            aResponse()
              .withStatus(500)))

        intercept[ApplicationHttpException]{
          Await.result(connector.getResponse, Duration.Inf)
        }
      }
    }
  }

}
