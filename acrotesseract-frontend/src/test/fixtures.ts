import type { Pose, PoseDetail, Transition, TransitionDetail } from '../api/types';

export const ground: Pose = {
  id: '5f9040d6-fdaf-444e-9e3b-83ae4da54843',
  name: 'Ground',
  descriptionMd: 'Starting position.',
};
export const bird: Pose = {
  id: '11111111-1111-4111-8111-111111111111',
  name: 'Front Bird',
  descriptionMd: 'Horizontal on the **base**’s feet.',
  imageUrl: 'https://example.com/bird.jpg',
};
export const throne: Pose = { id: '22222222-2222-4222-8222-222222222222', name: 'Throne' };

export const groundToBird: Transition = {
  id: 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
  name: 'Ground to Front Bird',
  descriptionMd: 'Standing entry.',
  poseFrom: ground.id,
  poseTo: bird.id,
  youtubeUrl: 'https://youtu.be/g8OhDBRwhSw?t=403',
};
export const birdToThrone: Transition = {
  id: 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb',
  name: 'Front Bird to Throne',
  poseFrom: bird.id,
  poseTo: throne.id,
};

export const poses: Pose[] = [bird, ground, throne];
export const transitions: Transition[] = [birdToThrone, groundToBird];

export const birdDetail: PoseDetail = { pose: bird, transitionsFrom: [birdToThrone], transitionsTo: [groundToBird] };
export const groundToBirdDetail: TransitionDetail = { transition: groundToBird, poseFrom: ground, poseTo: bird };
