package acrotesseract

import com.github.plokhotnyuk.jsoniter_scala.core.*

/** A transport-neutral HTTP request, so the same router serves Lambda and the local server.
  * Header names are lower-case.
  */
final case class Request(
    method: String,
    path: String,
    body: Option[String] = None,
    headers: Map[String, String] = Map.empty
):
  def header(name: String): Option[String] = headers.get(name.toLowerCase)

final case class Response(status: Int, body: String, headers: Map[String, String] = Map.empty)

object Response:
  private val jsonHeaders = Map("Content-Type" -> "application/json")

  def json[A](status: Int, value: A, extraHeaders: Map[String, String] = Map.empty)(using JsonValueCodec[A]): Response =
    Response(status, writeToString(value), jsonHeaders ++ extraHeaders)

  def error(status: Int, message: String): Response = json(status, ErrorBody(message))

  def error(e: WriteError): Response = e match
    case WriteError.Invalid(message)  => error(400, message)
    case WriteError.NotFound(message) => error(404, message)
    case WriteError.Conflict(message) => error(409, message)
