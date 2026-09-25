package acrotesseract

import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

import java.net.URI

/** Builds the router from environment variables. CDK sets them on the Lambda; sbt sets them for local runs.
  *
  *   - `STAGE`: reported by /api/health. `local` also turns writes on.
  *   - `POSES_TABLE`, `TRANSITIONS_TABLE`: use DynamoDB. Without them, serve the bundled data/data.json in memory.
  *   - `DYNAMODB_ENDPOINT`: optional, e.g. http://localhost:8000 for DynamoDB Local.
  *   - `WRITES_ENABLED`: `true` allows POST/PUT.
  */
object Wiring:
  def routerFromEnv(env: Map[String, String] = sys.env): Router =
    val stage = env.getOrElse("STAGE", "local")
    val writesEnabled = env.get("WRITES_ENABLED").map(_ == "true").getOrElse(stage == "local")
    Router(stage, repositoryFromEnv(env), writesEnabled)

  def repositoryFromEnv(env: Map[String, String] = sys.env): AcroRepository =
    (env.get("POSES_TABLE"), env.get("TRANSITIONS_TABLE")) match
      case (Some(poses), Some(transitions)) =>
        DynamoDbRepository(dynamoDbClient(env.get("DYNAMODB_ENDPOINT")), poses, transitions)
      case _ => StaticData.load()

  /** The URLConnection HTTP client starts faster than the default Apache one, which matters for Lambda cold starts. */
  def dynamoDbClient(endpoint: Option[String] = None): DynamoDbClient =
    val builder = DynamoDbClient.builder().httpClient(UrlConnectionHttpClient.create())
    endpoint.foreach(e => builder.endpointOverride(URI.create(e)))
    builder.build()
