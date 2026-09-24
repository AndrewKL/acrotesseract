package acrotesseract

import java.nio.charset.StandardCharsets.UTF_8
import java.util.UUID

class StaticDataSuite extends munit.FunSuite:
  private val poseA = "5f9040d6-fdaf-444e-9e3b-83ae4da54843"
  private val missing = "00000000-0000-4000-8000-000000000009"

  test("loads the bundled data/data.json"):
    val repo = StaticData.load()
    assert(repo.listPoses().nonEmpty)
    assert(repo.listTransitions().nonEmpty)
    val ground = repo.listPoses().find(_.name == "Ground")
    assert(ground.isDefined, "Ground is the starting pose")
    assert(repo.transitionsFrom(ground.get.id).nonEmpty, "Ground must have a way in to the graph")

  test("maps the RDS column names and skips unused fields"):
    val transitionId = "7d5e7a58-3a0c-4a52-9f3e-2f1d8c6b4a10"
    val json =
      s"""{"$$schema":"./data.schema.json",
        |"poses":[{"pose_id":"$poseA","created_ts":null,"name":"Ground","created_by":"A","image_url":null,"description_md":"Start"}],
        |"transitions":[{"transition_id":"$transitionId","name":"Loop","pose_from":"$poseA","pose_to":"$poseA","youtube_url":"https://youtu.be/x"}]}""".stripMargin
    val repo = StaticData.parse(json.getBytes(UTF_8))
    val a = UUID.fromString(poseA)
    assertEquals(repo.getPose(a), Some(Pose(a, "Ground", None, Some("Start"))))
    assertEquals(
      repo.getTransition(UUID.fromString(transitionId)),
      Some(Transition(UUID.fromString(transitionId), "Loop", None, a, a, Some("https://youtu.be/x")))
    )

  test("rejects duplicate ids, duplicate names and missing poses"):
    val json =
      s"""{"poses":[{"pose_id":"$poseA","name":"A"},{"pose_id":"$poseA","name":"A"}],
        |"transitions":[{"transition_id":"$missing","name":"T","pose_from":"$poseA","pose_to":"$missing"}]}""".stripMargin
    val e = intercept[IllegalArgumentException](StaticData.parse(json.getBytes(UTF_8)))
    assert(e.getMessage.contains(s"duplicate pose_id $poseA"), e.getMessage)
    assert(e.getMessage.contains("duplicate pose name 'A'"), e.getMessage)
    assert(e.getMessage.contains(s"refers to missing pose $missing"), e.getMessage)

  test("rejects ids that are not UUIDs"):
    val json = """{"poses":[{"pose_id":1,"name":"A"}],"transitions":[]}"""
    intercept[com.github.plokhotnyuk.jsoniter_scala.core.JsonReaderException](StaticData.parse(json.getBytes(UTF_8)))
