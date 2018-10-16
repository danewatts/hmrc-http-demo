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
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.http.Status._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ApplicationConnector @Inject()(
                                      http: WSHttp,
                                      config: Configuration
                                    ) {

  val port: Int = config.underlying.getInt("connector.port")
  private val host = s"http://localhost:$port"
  private[connectors] val url = "/example-frontend/value"
  protected[connectors] val defaultNotFoundResponse: HttpResponse = HttpResponse(200, None, Map(), Some("""{"response": false}"""))

  private implicit val httpReads: HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
    def read(method: String, url: String, response: HttpResponse): HttpResponse = {
      response
    }
  }

  def getResponse(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.GET[HttpResponse](host + url) map {
      response ⇒
        response.status match {
        case OK ⇒
          response
        case NOT_FOUND ⇒
          defaultNotFoundResponse
        case _ ⇒
          throw new ApplicationHttpException("Failed")
      }
    }
  }
}

class ApplicationHttpException(message: String) extends Exception(message)
