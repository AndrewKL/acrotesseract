package acrotesseract

import java.util.UUID

class RouterWriteSuite extends munit.FunSuite:
  private val json = Map("content-type" -> "application/json")
  private def id(n: Int) = UUID.fromString(f"00000000-0000-4000-8000-$n%012d")
  private val ground = Pose(id(1), "Ground", None, None, 1)
  private val bird = Pose(id(2), "Front Bird", None, None, 1)

  private def router(writesEnabled: Boolean = true) =
    Router("test", InMemoryRepository(List(ground, bird), Nil), writesEnabled)

  private def send(r: Router, method: String, path: String, body: String, headers: Map[String, String] = json) =
    r.handle(Request(method, path, Some(body), headers))

  test("POST /api/poses creates a pose and returns 201 with a Location header"):
    val r = router()
    val res = send(r, "POST", "/api/poses", """{"name":" Throne ","descriptionMd":"Sitting on the feet."}""")
    assertEquals(res.status, 201, res.body)
    assert(res.body.contains(""""name":"Throne""""), res.body)
    assert(res.body.contains(""""version":1"""), res.body)
    val location = res.headers("Location")
    assert(location.startsWith("/api/poses/"), location)
    assertEquals(r.handle(Request("GET", location)).status, 200)

  test("PUT /api/poses/{id} needs the current version"):
    val r = router()
    val path = s"/api/poses/${bird.id}"
    assertEquals(send(r, "PUT", path, """{"name":"Bird"}""").status, 400)
    val ok = send(r, "PUT", path, """{"name":"Bird","version":1}""")
    assertEquals(ok.status, 200, ok.body)
    assert(ok.body.contains(""""version":2"""), ok.body)
    val stale = send(r, "PUT", path, """{"name":"Bird again","version":1}""")
    assertEquals(stale.status, 409, stale.body)
    assert(stale.body.contains("changed by someone else"), stale.body)

  test("PUT to a missing pose is 404, and a duplicate name is 409"):
    val r = router()
    assertEquals(send(r, "PUT", s"/api/poses/${id(9)}", """{"name":"X","version":1}""").status, 404)
    assertEquals(send(r, "POST", "/api/poses", """{"name":"ground"}""").status, 409)

  test("POST /api/transitions checks that both poses exist"):
    val r = router()
    val ok = send(r, "POST", "/api/transitions", s"""{"name":"Up","poseFrom":"${ground.id}","poseTo":"${bird.id}"}""")
    assertEquals(ok.status, 201, ok.body)
    val missing = send(r, "POST", "/api/transitions", s"""{"name":"Nope","poseFrom":"${ground.id}","poseTo":"${id(9)}"}""")
    assertEquals(missing.status, 400)
    assert(missing.body.contains(s"To pose ${id(9)} does not exist"), missing.body)
    assertEquals(r.handle(Request("GET", s"/api/poses/${ground.id}")).body.contains("\"Up\""), true)

  test("PUT /api/transitions/{id} updates with the version"):
    val r = router()
    val created = send(r, "POST", "/api/transitions", s"""{"name":"Up","poseFrom":"${ground.id}","poseTo":"${bird.id}"}""")
    val path = created.headers("Location")
    val body = s"""{"name":"Up and back","poseFrom":"${bird.id}","poseTo":"${ground.id}","youtubeUrl":"https://youtu.be/g8OhDBRwhSw","version":1}"""
    val res = send(r, "PUT", path, body)
    assertEquals(res.status, 200, res.body)
    assert(res.body.contains(s""""poseFrom":"${bird.id}""""), res.body)

  test("bad bodies are 400"):
    val r = router()
    assertEquals(send(r, "POST", "/api/poses", "").status, 400)
    assertEquals(send(r, "POST", "/api/poses", "not json").status, 400)
    assertEquals(send(r, "POST", "/api/poses", """{"descriptionMd":"no name"}""").status, 400)
    assertEquals(send(r, "POST", "/api/transitions", """{"name":"T","poseFrom":"nope","poseTo":"nope"}""").status, 400)
    assertEquals(send(r, "PUT", "/api/poses/abc", """{"name":"X","version":1}""").status, 400)

  test("writes need a JSON content type"):
    val res = send(router(), "POST", "/api/poses", """{"name":"X"}""", Map("content-type" -> "text/plain"))
    assertEquals(res.status, 415)

  test("writes are refused when disabled"):
    val r = router(writesEnabled = false)
    val res = send(r, "POST", "/api/poses", """{"name":"X"}""")
    assertEquals(res.status, 403)
    assertEquals(res.body, """{"error":"Editing is disabled until sign-in is available"}""")
    assertEquals(r.handle(Request("GET", "/api/poses")).status, 200)
