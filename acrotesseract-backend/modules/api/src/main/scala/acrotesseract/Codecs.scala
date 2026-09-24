package acrotesseract

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

final case class Health(status: String, stage: String)
final case class ErrorBody(error: String)

/** GET /api/poses/{id}: the pose plus its outgoing and incoming transitions. */
final case class PoseDetail(pose: Pose, transitionsFrom: List[Transition], transitionsTo: List[Transition])

/** GET /api/transitions/{id}: the transition plus both endpoint poses. */
final case class TransitionDetail(transition: Transition, poseFrom: Pose, poseTo: Pose)

// API responses use camelCase field names. Optional fields that are None are omitted.
given healthCodec: JsonValueCodec[Health] = JsonCodecMaker.make
given errorBodyCodec: JsonValueCodec[ErrorBody] = JsonCodecMaker.make
given poseListCodec: JsonValueCodec[List[Pose]] = JsonCodecMaker.make
given transitionListCodec: JsonValueCodec[List[Transition]] = JsonCodecMaker.make
given poseDetailCodec: JsonValueCodec[PoseDetail] = JsonCodecMaker.make
given transitionDetailCodec: JsonValueCodec[TransitionDetail] = JsonCodecMaker.make
