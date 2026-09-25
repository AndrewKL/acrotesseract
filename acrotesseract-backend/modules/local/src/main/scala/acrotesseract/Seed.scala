package acrotesseract

import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest

import scala.jdk.CollectionConverters.*

/** Loads data/data.json into the DynamoDB tables named by POSES_TABLE and TRANSITIONS_TABLE.
  * Existing items (same id) are left alone, so it's safe to run again. Run with `npx nx seed acrotesseract-backend`
  * (prod) or `npx nx seed acrotesseract-backend -c local` (DynamoDB Local, where missing tables are created first).
  */
@main def seed(): Unit =
  val posesTable = sys.env.getOrElse("POSES_TABLE", sys.error("POSES_TABLE is not set"))
  val transitionsTable = sys.env.getOrElse("TRANSITIONS_TABLE", sys.error("TRANSITIONS_TABLE is not set"))
  val endpoint = sys.env.get("DYNAMODB_ENDPOINT")
  val ddb = Wiring.dynamoDbClient(endpoint)

  // Deployed tables come from CDK; only DynamoDB Local gets tables created here.
  if endpoint.isDefined then
    val existing = ddb.listTables(ListTablesRequest.builder().build()).tableNames().asScala.toSet
    if !existing.contains(posesTable) then
      DynamoDbTables.createAll(ddb, posesTable, transitionsTable)
      println(s"Created $posesTable and $transitionsTable in DynamoDB Local")

  val (poses, transitions) = StaticData.loadData()
  val (created, skipped) = DynamoDbRepository(ddb, posesTable, transitionsTable).seed(poses, transitions)
  println(s"Seeded $posesTable and $transitionsTable: $created items created, $skipped already existed")
