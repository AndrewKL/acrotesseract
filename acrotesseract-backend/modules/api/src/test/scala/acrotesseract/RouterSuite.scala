package acrotesseract

class RouterSuite extends munit.FunSuite:
  private val router = Router(
    "test",
    InMemoryPoseRepository(
      List(
        Pose(2, "Throne", None, None),
        Pose(1, "Bird", Some("https://example.com/bird.jpg"), None)
      )
    )
  )

  test("GET /api/health reports the stage"):
    val res = router.handle(Request("GET", "/api/health"))
    assertEquals(res.status, 200)
    assertEquals(res.body, """{"status":"ok","stage":"test"}""")
    assertEquals(res.headers.get("Content-Type"), Some("application/json"))

  test("GET /api/poses lists poses sorted by name"):
    val res = router.handle(Request("GET", "/api/poses"))
    assertEquals(res.status, 200)
    assert(res.body.indexOf("Bird") < res.body.indexOf("Throne"), res.body)

  test("unknown API routes return 404 JSON"):
    val res = router.handle(Request("DELETE", "/api/nope"))
    assertEquals(res.status, 404)
    assert(res.body.contains("No route for DELETE /api/nope"), res.body)
