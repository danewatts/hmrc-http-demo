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
import connectors.MyResponse.{Failure, Success}
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

sealed trait MyResponse

object MyResponse {

  final case class Success(response: Boolean) extends MyResponse
  final case class Failure(reason: String) extends MyResponse

  implicit lazy val httpReads: HttpReads[MyResponse] =
    new HttpReads[MyResponse] {
      override def read(method: String, url: String, response: HttpResponse): MyResponse =
        response.status match {
          case OK ⇒
            response.json.as[Success]
          case NOT_FOUND ⇒
            Failure(response.body)
          case status ⇒
            throw new ApplicationHttpException(s"Failed with status $status")
        }
    }
  implicit val successReads: Reads[Success] = Json.reads[Success]

}

@Singleton
class ApplicationConnector @Inject()(
                                      http: WSHttp,
                                      config: Configuration
                                    ) {

  val port: Int = config.underlying.getInt("connector.port")
  private val host = s"http://localhost:$port"
  private[connectors] val url = "/example-frontend/value"

  def getResponse(implicit hc: HeaderCarrier): Future[MyResponse] = {
    http.GET[MyResponse](host + url)
  }
}

class ApplicationHttpException(message: String) extends Exception(message)
