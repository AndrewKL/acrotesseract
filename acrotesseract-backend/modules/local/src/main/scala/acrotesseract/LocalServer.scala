package acrotesseract

import com.sun.net.httpserver.{HttpExchange, HttpServer}

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

/** Serves the API on http://localhost:8080 for local development. Vite proxies /api here. */
@main def runLocalServer(): Unit =
  val port = sys.env.get("PORT").map(_.toInt).getOrElse(8080)
  val router = Router.fromEnv()
  val server = HttpServer.create(InetSocketAddress("localhost", port), 0)
  server.createContext(
    "/",
    (exchange: HttpExchange) =>
      val body = String(exchange.getRequestBody.readAllBytes(), StandardCharsets.UTF_8)
      val req = Request(exchange.getRequestMethod, exchange.getRequestURI.getPath, Option(body).filter(_.nonEmpty))
      val res = router.handle(req)
      val bytes = res.body.getBytes(StandardCharsets.UTF_8)
      res.headers.foreach((k, v) => exchange.getResponseHeaders.add(k, v))
      exchange.sendResponseHeaders(res.status, bytes.length.toLong)
      exchange.getResponseBody.write(bytes)
      exchange.close()
  )
  server.start()
  println(s"Acro Tesseract API listening on http://localhost:$port")
