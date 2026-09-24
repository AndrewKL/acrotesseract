/** Mirrors the Scala API's JSON (acrotesseract-backend/modules/api). Ids are UUID strings. Absent optionals are omitted. */
export interface Pose {
  id: string;
  name: string;
  imageUrl?: string;
  descriptionMd?: string;
}

export interface Transition {
  id: string;
  name: string;
  descriptionMd?: string;
  poseFrom: string;
  poseTo: string;
  youtubeUrl?: string;
}

/** GET /api/poses/{id} */
export interface PoseDetail {
  pose: Pose;
  transitionsFrom: Transition[];
  transitionsTo: Transition[];
}

/** GET /api/transitions/{id} */
export interface TransitionDetail {
  transition: Transition;
  poseFrom: Pose;
  poseTo: Pose;
}
