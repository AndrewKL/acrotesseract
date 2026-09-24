package acrotesseract

import com.github.plokhotnyuk.jsoniter_scala.core.*

/** A transport-neutral HTTP request, so the same router serves Lambda and the local server. */
final case class Request(method: String, path: String, body: Option[String] = None)

final case class Response(status: Int, body: String, headers: Map[String, String] = Map.empty)

object Response:
  private val jsonHeaders = Map("Content-Type" -> "application/json")

  def json[A](status: Int, value: A)(using JsonValueCodec[A]): Response =
    Response(status, writeToString(value), jsonHeaders)

  def error(status: Int, message: String): Response = json(status, ErrorBody(message))
