package acrotesseract

import java.util.UUID

class RouterSuite extends munit.FunSuite:
  private def id(n: Int) = UUID.fromString(f"00000000-0000-4000-8000-$n%012d")

  private val ground = Pose(id(1), "Ground", None, None, 1)
  private val bird = Pose(id(2), "Front Bird", None, Some("Horizontal on the base's feet."), 1)
  private val throne = Pose(id(3), "Throne", None, None, 1)

  private val router = Router(
    "test",
    InMemoryRepository(
      List(throne, ground, bird),
      List(
        Transition(id(11), "Ground to Front Bird", None, ground.id, bird.id, Some("https://youtu.be/g8OhDBRwhSw?t=403"), 1),
        Transition(id(12), "Front Bird to Throne", None, bird.id, throne.id, None, 1),
        Transition(id(13), "Throne to Front Bird", None, throne.id, bird.id, None, 1)
      )
    )
  )

  private def get(path: String) = router.handle(Request("GET", path))

  test("GET /api/health reports the stage"):
    val res = get("/api/health")
    assertEquals(res.status, 200)
    assertEquals(res.body, """{"status":"ok","stage":"test"}""")
    assertEquals(res.headers.get("Content-Type"), Some("application/json"))

  test("GET /api/poses lists poses sorted by name"):
    val res = get("/api/poses")
    assertEquals(res.status, 200)
    val order = List("Front Bird", "Ground", "Throne").map(res.body.indexOf)
    assertEquals(order, order.sorted, res.body)

  test("GET /api/poses/{id} includes transitions from and to the pose, with UUID ids"):
    val res = get(s"/api/poses/${bird.id}")
    assertEquals(res.status, 200)
    assertEquals(
      res.body,
      s"""{"pose":{"id":"${id(2)}","name":"Front Bird","descriptionMd":"Horizontal on the base's feet.","version":1},""" +
        s""""transitionsFrom":[{"id":"${id(12)}","name":"Front Bird to Throne","poseFrom":"${id(2)}","poseTo":"${id(3)}","version":1}],""" +
        s""""transitionsTo":[{"id":"${id(11)}","name":"Ground to Front Bird","poseFrom":"${id(1)}","poseTo":"${id(2)}","youtubeUrl":"https://youtu.be/g8OhDBRwhSw?t=403","version":1},""" +
        s"""{"id":"${id(13)}","name":"Throne to Front Bird","poseFrom":"${id(3)}","poseTo":"${id(2)}","version":1}]}"""
    )

  test("GET /api/poses/{id} always includes both transition lists, even when empty"):
    val lonely = Pose(id(4), "Lonely", None, None, 1)
    val r = Router("test", InMemoryRepository(List(lonely), Nil))
    assertEquals(
      r.handle(Request("GET", s"/api/poses/${lonely.id}")).body,
      s"""{"pose":{"id":"${id(4)}","name":"Lonely","version":1},"transitionsFrom":[],"transitionsTo":[]}"""
    )

  test("GET /api/transitions/{id} includes both poses"):
    val res = get(s"/api/transitions/${id(12)}")
    assertEquals(res.status, 200)
    assert(res.body.startsWith(s"""{"transition":{"id":"${id(12)}","name":"Front Bird to Throne""""), res.body)
    assert(res.body.contains(s""""poseFrom":{"id":"${id(2)}","name":"Front Bird""""), res.body)
    assert(res.body.contains(s""""poseTo":{"id":"${id(3)}","name":"Throne","version":1}"""), res.body)

  test("GET /api/transitions lists transitions sorted by name"):
    val res = get("/api/transitions")
    assertEquals(res.status, 200)
    assert(res.body.indexOf("Front Bird to Throne") < res.body.indexOf("Ground to Front Bird"), res.body)

  test("ids are matched case-insensitively"):
    assertEquals(get(s"/api/poses/${bird.id.toString.toUpperCase}").status, 200)

  test("unknown ids return 404 and malformed ids return 400"):
    assertEquals(get(s"/api/poses/${id(99)}").status, 404)
    assertEquals(get(s"/api/transitions/${id(99)}").status, 404)
    assertEquals(get("/api/poses/abc").status, 400)
    assertEquals(get("/api/poses/12").status, 400)
    assertEquals(get("/api/transitions/1-1-1-1-1").status, 400)

  test("unknown API routes return 404 JSON"):
    val res = router.handle(Request("DELETE", "/api/nope"))
    assertEquals(res.status, 404)
    assert(res.body.contains("No route for DELETE /api/nope"), res.body)
