package acrotesseract

import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

import scala.jdk.CollectionConverters.*

/** The table definitions, for DynamoDB Local (tests and local dev). Must match acrotesseract-cdk StorageStack. */
object DynamoDbTables:
  private def attr(name: String) =
    AttributeDefinition.builder().attributeName(name).attributeType(ScalarAttributeType.S).build()

  private def key(name: String, keyType: KeyType) =
    KeySchemaElement.builder().attributeName(name).keyType(keyType).build()

  private def index(name: String, hash: String, range: Option[String], projection: ProjectionType) =
    GlobalSecondaryIndex
      .builder()
      .indexName(name)
      .keySchema((key(hash, KeyType.HASH) :: range.map(key(_, KeyType.RANGE)).toList).asJava)
      .projection(Projection.builder().projectionType(projection).build())
      .build()

  def createAll(ddb: DynamoDbClient, posesTable: String, transitionsTable: String): Unit =
    ddb.createTable(
      CreateTableRequest
        .builder()
        .tableName(posesTable)
        .billingMode(BillingMode.PAY_PER_REQUEST)
        .attributeDefinitions(attr("poseId"), attr("nameLower"))
        .keySchema(key("poseId", KeyType.HASH))
        .globalSecondaryIndexes(index("byName", "nameLower", None, ProjectionType.KEYS_ONLY))
        .build()
    )
    ddb.createTable(
      CreateTableRequest
        .builder()
        .tableName(transitionsTable)
        .billingMode(BillingMode.PAY_PER_REQUEST)
        .attributeDefinitions(attr("transitionId"), attr("nameLower"), attr("poseFrom"), attr("poseTo"))
        .keySchema(key("transitionId", KeyType.HASH))
        .globalSecondaryIndexes(
          index("byName", "nameLower", None, ProjectionType.KEYS_ONLY),
          index("byPoseFrom", "poseFrom", Some("nameLower"), ProjectionType.ALL),
          index("byPoseTo", "poseTo", Some("nameLower"), ProjectionType.ALL)
        )
        .build()
    )

  def deleteAll(ddb: DynamoDbClient, tables: String*): Unit =
    tables.foreach(t => ddb.deleteTable(DeleteTableRequest.builder().tableName(t).build()))
