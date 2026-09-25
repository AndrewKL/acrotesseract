package acrotesseract

import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

import java.net.URI
import java.util.UUID

/** Runs the repository contract against DynamoDB Local. Skipped unless DYNAMODB_ENDPOINT is set, e.g.:
  * {{{
  * podman run -d --rm -p 8000:8000 --name ddb amazon/dynamodb-local
  * DYNAMODB_ENDPOINT=http://localhost:8000 sbt store/test
  * }}}
  */
class DynamoDbRepositorySuite extends RepositoryContract:
  private val endpoint = sys.env.get("DYNAMODB_ENDPOINT")

  private lazy val ddb = DynamoDbClient
    .builder()
    .httpClient(UrlConnectionHttpClient.create())
    .endpointOverride(URI.create(endpoint.get))
    .region(Region.US_WEST_2)
    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("local", "local")))
    .build()

  /** Each test gets its own pair of tables, created with the same schema as the CDK stack. */
  private def withTables(body: (String, String) => Unit): Unit =
    val suffix = UUID.randomUUID().toString.take(8)
    val (poses, transitions) = (s"poses-$suffix", s"transitions-$suffix")
    DynamoDbTables.createAll(ddb, poses, transitions)
    try body(poses, transitions)
    finally DynamoDbTables.deleteAll(ddb, poses, transitions)

  def withRepo(name: String)(body: AcroRepository => Unit): Unit =
    test(name) {
      assume(endpoint.isDefined, "DYNAMODB_ENDPOINT is not set")
      withTables((p, t) => body(DynamoDbRepository(ddb, p, t)))
    }

  test("seed writes data.json once and skips existing items on the next run") {
    assume(endpoint.isDefined, "DYNAMODB_ENDPOINT is not set")
    withTables { (p, t) =>
      val repo = DynamoDbRepository(ddb, p, t)
      val (poses, transitions) = StaticData.loadData()
      assertEquals(repo.seed(poses, transitions), (poses.size + transitions.size, 0))
      assertEquals(repo.seed(poses, transitions), (0, poses.size + transitions.size))
      assertEquals(repo.listPoses(), poses.sortBy(_.name.toLowerCase))
      assertEquals(repo.listTransitions().size, transitions.size)
      val ground = poses.find(_.name == "Ground").get
      assertEquals(repo.transitionsFrom(ground.id), transitions.filter(_.poseFrom == ground.id).sortBy(_.name.toLowerCase))
    }
  }
