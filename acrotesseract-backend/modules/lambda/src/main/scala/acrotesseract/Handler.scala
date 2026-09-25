package acrotesseract

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.{APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse}

import scala.jdk.CollectionConverters.*

/** Lambda entrypoint for the API Gateway HTTP API (payload format 2.0).
  *
  * The router and its DynamoDB client are built in the constructor so SnapStart captures them in the snapshot.
  */
class Handler extends RequestHandler[APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse]:
  private val router = Wiring.routerFromEnv()

  override def handleRequest(event: APIGatewayV2HTTPEvent, context: Context): APIGatewayV2HTTPResponse =
    val http = event.getRequestContext.getHttp
    val body = Option(event.getBody).map { b =>
      if event.getIsBase64Encoded then String(java.util.Base64.getDecoder.decode(b), "UTF-8") else b
    }
    // Payload v2 header names are already lower-case.
    val headers = Option(event.getHeaders).map(_.asScala.toMap).getOrElse(Map.empty)
    val res = router.handle(Request(http.getMethod, http.getPath, body, headers))
    APIGatewayV2HTTPResponse
      .builder()
      .withStatusCode(res.status)
      .withHeaders(res.headers.asJava)
      .withBody(res.body)
      .build()
