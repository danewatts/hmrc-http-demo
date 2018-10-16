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
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ApplicationConnectorSpec extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  ".getResponse" should {
    "return full response" when {
      "response status is OK" in {
        val http: WSHttp = mock[WSHttp]
        val connector: ApplicationConnector = new ApplicationConnector(http)
        val response: HttpResponse = HttpResponse(200, None, Map(),Some("""{"response": true}"""))
        when(http.GET[HttpResponse](any())(any(), any(), any())) thenReturn Future.successful(response)

        whenReady(connector.getResponse) {
          _ mustBe response
        }
      }
    }

    "throw HttpException" when {
      "response status is 204" in {
        val http: WSHttp = mock[WSHttp]
        val connector: ApplicationConnector = new ApplicationConnector(http)
        val response: HttpResponse = HttpResponse(204, None, Map(),None)
        when(http.GET[HttpResponse](any())(any(), any(), any())) thenReturn Future.successful(response)

        intercept[ApplicationHttpException]{
          Await.result(connector.getResponse, Duration.Inf)
        }
      }

      "response status is 404" in {
        val http: WSHttp = mock[WSHttp]
        val connector: ApplicationConnector = new ApplicationConnector(http)
        val response: HttpResponse = HttpResponse(404, None, Map(),None)
        when(http.GET[HttpResponse](any())(any(), any(), any())) thenReturn Future.successful(response)

        Await.result(connector.getResponse, Duration.Inf) mustBe connector.defaultNotFoundResponse
      }

      "response status is 500" in {
        val http: WSHttp = mock[WSHttp]
        val connector: ApplicationConnector = new ApplicationConnector(http)
        val response: HttpResponse = HttpResponse(500, None, Map(),None)
        when(http.GET[HttpResponse](any())(any(), any(), any())) thenReturn Future.successful(response)

        intercept[ApplicationHttpException]{
          Await.result(connector.getResponse, Duration.Inf)
        }
      }
    }
  }

}
