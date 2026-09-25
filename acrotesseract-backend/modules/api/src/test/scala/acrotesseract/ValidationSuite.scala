package acrotesseract

import java.util.UUID

class ValidationSuite extends munit.FunSuite:
  private val id = UUID.randomUUID()

  test("trims text and drops blank optional fields"):
    assertEquals(
      Validation.pose(PoseBody("  Front Bird ", Some("  "), Some(" Flying. "), None)),
      Right(PoseInput("Front Bird", None, Some("Flying.")))
    )

  test("requires a name within the length limit"):
    assertEquals(Validation.pose(PoseBody("   ", None, None, None)), Left(WriteError.Invalid("name is required")))
    assertEquals(
      Validation.pose(PoseBody("x" * 257, None, None, None)),
      Left(WriteError.Invalid("name must be at most 256 characters"))
    )

  test("requires http(s) image URLs"):
    assertEquals(
      Validation.pose(PoseBody("A", Some("javascript:alert(1)"), None, None)),
      Left(WriteError.Invalid("imageUrl must be an http(s) URL"))
    )
    assert(Validation.pose(PoseBody("A", Some("https://example.com/a.jpg"), None, None)).isRight)

  test("requires YouTube links for transition videos"):
    def withVideo(url: String) = Validation.transition(TransitionBody("T", None, id, id, Some(url), None))
    assert(withVideo("https://youtu.be/g8OhDBRwhSw?t=403").isRight)
    assert(withVideo("https://www.youtube.com/watch?v=g8OhDBRwhSw").isRight)
    assertEquals(withVideo("https://vimeo.com/1"), Left(WriteError.Invalid("youtubeUrl must be a YouTube link")))

  test("limits descriptions to 10000 characters"):
    assert(Validation.pose(PoseBody("A", None, Some("x" * 10000), None)).isRight)
    assert(Validation.pose(PoseBody("A", None, Some("x" * 10001), None)).isLeft)
