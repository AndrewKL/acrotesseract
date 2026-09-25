package acrotesseract

import java.util.UUID

/** Behavior every AcroRepository must have. Run against InMemoryRepository here and DynamoDbRepository in store. */
abstract class RepositoryContract extends munit.FunSuite:
  /** A fresh, empty repository per test. */
  def withRepo(name: String)(body: AcroRepository => Unit): Unit

  private def poseInput(name: String) = PoseInput(name, None, Some(s"$name description"))
  private def transitionInput(name: String, from: UUID, to: UUID) =
    TransitionInput(name, None, from, to, Some("https://youtu.be/g8OhDBRwhSw?t=403"))

  private def created[A](e: Either[WriteError, A]): A = e.fold(err => fail(s"unexpected $err"), identity)

  withRepo("creates a pose at version 1 and reads it back") { repo =>
    val pose = created(repo.createPose(PoseInput("Ground", Some("https://example.com/g.jpg"), Some("Start"))))
    assertEquals(pose.version, 1L)
    assertEquals(repo.getPose(pose.id), Some(pose))
    assertEquals(repo.listPoses(), List(pose))
  }

  withRepo("rejects a duplicate pose name, ignoring case") { repo =>
    created(repo.createPose(poseInput("Front Bird")))
    assertEquals(repo.createPose(poseInput("front bird")), Left(RepositoryErrors.poseNameTaken("front bird")))
  }

  withRepo("updates a pose when the version matches, and bumps the version") { repo =>
    val pose = created(repo.createPose(poseInput("Bird")))
    val updated = created(repo.updatePose(pose.id, PoseInput("Front Bird", None, None), expectedVersion = 1))
    assertEquals(updated, Pose(pose.id, "Front Bird", None, None, 2))
    assertEquals(repo.getPose(pose.id), Some(updated))
    // Renaming a pose to a different capitalization of its own name is fine.
    created(repo.updatePose(pose.id, PoseInput("FRONT BIRD", None, None), expectedVersion = 2))
  }

  withRepo("refuses a pose update with a stale version") { repo =>
    val pose = created(repo.createPose(poseInput("Bird")))
    created(repo.updatePose(pose.id, poseInput("Bird"), 1))
    assert(repo.updatePose(pose.id, poseInput("Bird 2"), 1).left.exists(_.isInstanceOf[WriteError.Conflict]))
    assertEquals(repo.getPose(pose.id).map(_.version), Some(2L))
  }

  withRepo("refuses renaming a pose to another pose's name, and updating a missing pose") { repo =>
    created(repo.createPose(poseInput("Star")))
    val bird = created(repo.createPose(poseInput("Bird")))
    assertEquals(repo.updatePose(bird.id, poseInput("STAR"), 1), Left(RepositoryErrors.poseNameTaken("STAR")))
    val missing = UUID.randomUUID()
    assertEquals(repo.updatePose(missing, poseInput("X"), 1), Left(RepositoryErrors.poseNotFound(missing)))
  }

  withRepo("creates transitions between existing poses, including self-loops") { repo =>
    val ground = created(repo.createPose(poseInput("Ground")))
    val bird = created(repo.createPose(poseInput("Front Bird")))
    val up = created(repo.createTransition(transitionInput("Ground to Front Bird", ground.id, bird.id)))
    val loop = created(repo.createTransition(transitionInput("Bird Wiggle", bird.id, bird.id)))
    assertEquals(up.version, 1L)
    assertEquals(repo.getTransition(up.id), Some(up))
    assertEquals(repo.listTransitions().map(_.name), List("Bird Wiggle", "Ground to Front Bird"))
    assertEquals(repo.transitionsFrom(ground.id), List(up))
    assertEquals(repo.transitionsTo(bird.id).map(_.name), List("Bird Wiggle", "Ground to Front Bird"))
    assertEquals(repo.transitionsFrom(bird.id), List(loop))
  }

  withRepo("refuses transitions to or from a missing pose, and duplicate names") { repo =>
    val bird = created(repo.createPose(poseInput("Front Bird")))
    val missing = UUID.randomUUID()
    assertEquals(
      repo.createTransition(transitionInput("A", missing, bird.id)),
      Left(RepositoryErrors.missingPose("From", missing))
    )
    assertEquals(
      repo.createTransition(transitionInput("B", bird.id, missing)),
      Left(RepositoryErrors.missingPose("To", missing))
    )
    assertEquals(repo.listTransitions(), Nil)
    created(repo.createTransition(transitionInput("Wiggle", bird.id, bird.id)))
    assertEquals(
      repo.createTransition(transitionInput("wiggle", bird.id, bird.id)),
      Left(RepositoryErrors.transitionNameTaken("wiggle"))
    )
  }

  withRepo("updates a transition with version checks and pose checks") { repo =>
    val ground = created(repo.createPose(poseInput("Ground")))
    val bird = created(repo.createPose(poseInput("Front Bird")))
    val throne = created(repo.createPose(poseInput("Throne")))
    val t = created(repo.createTransition(transitionInput("Up", ground.id, bird.id)))

    val moved = created(repo.updateTransition(t.id, transitionInput("Up to Throne", ground.id, throne.id), 1))
    assertEquals(moved.version, 2L)
    assertEquals(repo.transitionsTo(bird.id), Nil)
    assertEquals(repo.transitionsTo(throne.id), List(moved))

    assert(repo.updateTransition(t.id, transitionInput("Stale", ground.id, bird.id), 1).left.exists(_.isInstanceOf[WriteError.Conflict]))
    val missing = UUID.randomUUID()
    assertEquals(
      repo.updateTransition(t.id, transitionInput("Broken", ground.id, missing), 2),
      Left(RepositoryErrors.missingPose("To", missing))
    )
    assertEquals(repo.getTransition(t.id), Some(moved))
    assertEquals(
      repo.updateTransition(missing, transitionInput("X", ground.id, bird.id), 1),
      Left(RepositoryErrors.transitionNotFound(missing))
    )
  }

class InMemoryRepositorySuite extends RepositoryContract:
  def withRepo(name: String)(body: AcroRepository => Unit): Unit =
    test(name)(body(InMemoryRepository(Nil, Nil)))
