package acrotesseract

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.{APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse}

import scala.jdk.CollectionConverters.*

/** Lambda entrypoint for the API Gateway HTTP API (payload format 2.0).
  *
  * The router is built in the constructor so SnapStart captures it in the snapshot.
  */
class Handler extends RequestHandler[APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse]:
  private val router = Router.fromEnv()

  override def handleRequest(event: APIGatewayV2HTTPEvent, context: Context): APIGatewayV2HTTPResponse =
    val http = event.getRequestContext.getHttp
    val body = Option(event.getBody).map { b =>
      if event.getIsBase64Encoded then String(java.util.Base64.getDecoder.decode(b), "UTF-8") else b
    }
    val res = router.handle(Request(http.getMethod, http.getPath, body))
    APIGatewayV2HTTPResponse
      .builder()
      .withStatusCode(res.status)
      .withHeaders(res.headers.asJava)
      .withBody(res.body)
      .build()
