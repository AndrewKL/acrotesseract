package acrotesseract

import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

import java.time.Instant
import java.util.UUID
import scala.jdk.CollectionConverters.*

/** Poses and transitions in two DynamoDB tables (see acrotesseract-cdk StorageStack and DynamoDbTables).
  *
  *   - Poses: key `poseId`; GSI `byName` on `nameLower` (keys only) for the unique-name check.
  *   - Transitions: key `transitionId`; GSIs `byName`, `byPoseFrom` and `byPoseTo` (poseFrom/poseTo + nameLower).
  *
  * Every item carries `version` (optimistic locking), `createdAt` and `updatedAt`. Unique names are checked with a
  * query on `byName` before writing. Two editors creating the same name in the same second could both succeed;
  * that's accepted while there are only a couple of editors.
  */
final class DynamoDbRepository(
    ddb: DynamoDbClient,
    posesTable: String,
    transitionsTable: String,
    clock: () => Instant = () => Instant.now()
) extends AcroRepository:
  import DynamoDbRepository.*
  import RepositoryErrors.*

  // --- Reads ---

  def listPoses(): List[Pose] = scan(posesTable).map(toPose).sortBy(_.name.toLowerCase)

  def getPose(id: UUID): Option[Pose] = get(posesTable, "poseId", id).map(toPose)

  def listTransitions(): List[Transition] = scan(transitionsTable).map(toTransition).sortBy(_.name.toLowerCase)

  def getTransition(id: UUID): Option[Transition] = get(transitionsTable, "transitionId", id).map(toTransition)

  def transitionsFrom(poseId: UUID): List[Transition] = queryByPose("byPoseFrom", "poseFrom", poseId)

  def transitionsTo(poseId: UUID): List[Transition] = queryByPose("byPoseTo", "poseTo", poseId)

  // --- Writes ---

  def createPose(input: PoseInput): Either[WriteError, Pose] =
    if nameUsed(posesTable, "poseId", input.name, except = None) then Left(poseNameTaken(input.name))
    else
      val pose = Pose(UUID.randomUUID(), input.name, input.imageUrl, input.descriptionMd, 1)
      val now = clock().toString
      putNew(posesTable, "poseId", poseItem(pose) ++ timestamps(now, now))
      Right(pose)

  def updatePose(id: UUID, input: PoseInput, expectedVersion: Long): Either[WriteError, Pose] =
    get(posesTable, "poseId", id) match
      case None                                                     => Left(poseNotFound(id))
      case Some(item) if version(item) != expectedVersion           => Left(staleVersion("This pose", expectedVersion, version(item)))
      case Some(_) if nameUsed(posesTable, "poseId", input.name, Some(id)) => Left(poseNameTaken(input.name))
      case Some(item) =>
        val pose = Pose(id, input.name, input.imageUrl, input.descriptionMd, expectedVersion + 1)
        val put = versionedPut(posesTable, poseItem(pose) ++ timestamps(item("createdAt").s(), clock().toString), expectedVersion)
        try
          ddb.putItem(put)
          Right(pose)
        catch case _: ConditionalCheckFailedException => Left(staleVersion("This pose", expectedVersion, expectedVersion + 1))

  def createTransition(input: TransitionInput): Either[WriteError, Transition] =
    if nameUsed(transitionsTable, "transitionId", input.name, except = None) then Left(transitionNameTaken(input.name))
    else
      val t = Transition(UUID.randomUUID(), input.name, input.descriptionMd, input.poseFrom, input.poseTo, input.youtubeUrl, 1)
      val now = clock().toString
      val put = Put
        .builder()
        .tableName(transitionsTable)
        .item((transitionItem(t) ++ timestamps(now, now)).asJava)
        .conditionExpression("attribute_not_exists(transitionId)")
        .build()
      writeTransition(input, put, onPutFailed = transitionNameTaken(input.name)).map(_ => t)

  def updateTransition(id: UUID, input: TransitionInput, expectedVersion: Long): Either[WriteError, Transition] =
    get(transitionsTable, "transitionId", id) match
      case None                                           => Left(transitionNotFound(id))
      case Some(item) if version(item) != expectedVersion => Left(staleVersion("This transition", expectedVersion, version(item)))
      case Some(_) if nameUsed(transitionsTable, "transitionId", input.name, Some(id)) =>
        Left(transitionNameTaken(input.name))
      case Some(item) =>
        val t = Transition(id, input.name, input.descriptionMd, input.poseFrom, input.poseTo, input.youtubeUrl, expectedVersion + 1)
        val put = Put
          .builder()
          .tableName(transitionsTable)
          .item((transitionItem(t) ++ timestamps(item("createdAt").s(), clock().toString)).asJava)
          .conditionExpression("version = :expected")
          .expressionAttributeValues(Map(":expected" -> n(expectedVersion)).asJava)
          .build()
        writeTransition(input, put, onPutFailed = staleVersion("This transition", expectedVersion, expectedVersion + 1))
          .map(_ => t)

  /** Writes the transition in one transaction with existence checks on both poses, so it can't point at a missing
    * pose. A self-loop checks its pose once, since a transaction can't touch the same item twice.
    */
  private def writeTransition(input: TransitionInput, put: Put, onPutFailed: WriteError): Either[WriteError, Unit] =
    val checks =
      List("From" -> input.poseFrom) ++ Option.when(input.poseTo != input.poseFrom)("To" -> input.poseTo)
    val items =
      checks.map((_, poseId) =>
        TransactWriteItem.builder().conditionCheck(poseExists(poseId)).build()
      ) :+ TransactWriteItem.builder().put(put).build()
    try
      ddb.transactWriteItems(TransactWriteItemsRequest.builder().transactItems(items.asJava).build())
      Right(())
    catch
      case e: TransactionCanceledException =>
        val failed = e.cancellationReasons().asScala.map(_.code()).map(_ == "ConditionalCheckFailed").toList
        val missing = checks.zip(failed).collectFirst { case ((field, poseId), true) => missingPose(field, poseId) }
        Left(missing.getOrElse(if failed.lastOption.contains(true) then onPutFailed else throw e))

  private def poseExists(poseId: UUID): ConditionCheck =
    ConditionCheck
      .builder()
      .tableName(posesTable)
      .key(Map("poseId" -> s(poseId.toString)).asJava)
      .conditionExpression("attribute_exists(poseId)")
      .build()

  // --- Seeding ---

  /** Writes poses and transitions that don't exist yet, keeping their ids. Returns (created, skipped). */
  def seed(poses: List[Pose], transitions: List[Transition]): (Int, Int) =
    val now = clock().toString
    val results =
      poses.map(p => putIfAbsent(posesTable, "poseId", poseItem(p) ++ timestamps(now, now))) ++
        transitions.map(t => putIfAbsent(transitionsTable, "transitionId", transitionItem(t) ++ timestamps(now, now)))
    (results.count(identity), results.count(!_))

  private def putIfAbsent(table: String, keyName: String, item: Item): Boolean =
    try
      putNew(table, keyName, item)
      true
    catch case _: ConditionalCheckFailedException => false

  // --- Helpers ---

  private def get(table: String, keyName: String, id: UUID): Option[Item] =
    val res = ddb.getItem(
      GetItemRequest.builder().tableName(table).key(Map(keyName -> s(id.toString)).asJava).consistentRead(true).build()
    )
    Option.when(res.hasItem && !res.item().isEmpty)(res.item().asScala.toMap)

  private def scan(table: String): List[Item] =
    ddb.scanPaginator(ScanRequest.builder().tableName(table).build()).items().asScala.map(_.asScala.toMap).toList

  private def queryByPose(index: String, attribute: String, poseId: UUID): List[Transition] =
    val request = QueryRequest
      .builder()
      .tableName(transitionsTable)
      .indexName(index)
      .keyConditionExpression(s"$attribute = :pose")
      .expressionAttributeValues(Map(":pose" -> s(poseId.toString)).asJava)
      .build()
    // The index sort key is nameLower, so results come back sorted by name.
    ddb.queryPaginator(request).items().asScala.map(i => toTransition(i.asScala.toMap)).toList

  private def nameUsed(table: String, keyName: String, name: String, except: Option[UUID]): Boolean =
    val request = QueryRequest
      .builder()
      .tableName(table)
      .indexName("byName")
      .keyConditionExpression("nameLower = :name")
      .expressionAttributeValues(Map(":name" -> s(name.toLowerCase)).asJava)
      .build()
    ddb.query(request).items().asScala.exists(i => !except.map(_.toString).contains(i.get(keyName).s()))

  private def putNew(table: String, keyName: String, item: Item): Unit =
    ddb.putItem(
      PutItemRequest
        .builder()
        .tableName(table)
        .item(item.asJava)
        .conditionExpression(s"attribute_not_exists($keyName)")
        .build()
    )

  private def versionedPut(table: String, item: Item, expectedVersion: Long): PutItemRequest =
    PutItemRequest
      .builder()
      .tableName(table)
      .item(item.asJava)
      .conditionExpression("version = :expected")
      .expressionAttributeValues(Map(":expected" -> n(expectedVersion)).asJava)
      .build()

object DynamoDbRepository:
  type Item = Map[String, AttributeValue]

  private def s(value: String): AttributeValue = AttributeValue.fromS(value)
  private def n(value: Long): AttributeValue = AttributeValue.fromN(value.toString)

  /** Optional attributes are left off the item rather than stored empty. */
  private def opt(name: String, value: Option[String]): Item = value.map(v => name -> s(v)).toMap

  private def timestamps(createdAt: String, updatedAt: String): Item =
    Map("createdAt" -> s(createdAt), "updatedAt" -> s(updatedAt))

  private def version(item: Item): Long = item.get("version").map(_.n().toLong).getOrElse(1L)

  def poseItem(p: Pose): Item =
    Map(
      "poseId" -> s(p.id.toString),
      "name" -> s(p.name),
      "nameLower" -> s(p.name.toLowerCase),
      "version" -> n(p.version)
    ) ++ opt("imageUrl", p.imageUrl) ++ opt("descriptionMd", p.descriptionMd)

  def transitionItem(t: Transition): Item =
    Map(
      "transitionId" -> s(t.id.toString),
      "name" -> s(t.name),
      "nameLower" -> s(t.name.toLowerCase),
      "poseFrom" -> s(t.poseFrom.toString),
      "poseTo" -> s(t.poseTo.toString),
      "version" -> n(t.version)
    ) ++ opt("descriptionMd", t.descriptionMd) ++ opt("youtubeUrl", t.youtubeUrl)

  def toPose(i: Item): Pose =
    Pose(
      UUID.fromString(i("poseId").s()),
      i("name").s(),
      i.get("imageUrl").map(_.s()),
      i.get("descriptionMd").map(_.s()),
      version(i)
    )

  def toTransition(i: Item): Transition =
    Transition(
      UUID.fromString(i("transitionId").s()),
      i("name").s(),
      i.get("descriptionMd").map(_.s()),
      UUID.fromString(i("poseFrom").s()),
      UUID.fromString(i("poseTo").s()),
      i.get("youtubeUrl").map(_.s()),
      version(i)
    )
