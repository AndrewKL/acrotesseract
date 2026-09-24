import { useQuery } from '@tanstack/react-query';
import { useMemo } from 'react';
import { api } from './client';
import type { Pose } from './types';

export const queryKeys = {
  poses: ['poses'] as const,
  pose: (id: string) => ['poses', id] as const,
  transitions: ['transitions'] as const,
  transition: (id: string) => ['transitions', id] as const,
};

export function usePoses() {
  return useQuery({ queryKey: queryKeys.poses, queryFn: api.listPoses });
}

export function usePose(id: string) {
  return useQuery({ queryKey: queryKeys.pose(id), queryFn: () => api.getPose(id) });
}

export function useTransitions() {
  return useQuery({ queryKey: queryKeys.transitions, queryFn: api.listTransitions });
}

export function useTransition(id: string) {
  return useQuery({ queryKey: queryKeys.transition(id), queryFn: () => api.getTransition(id) });
}

/** Pose lookup by id, for naming the other end of a transition. Empty until the list loads. */
export function usePosesById(): Map<string, Pose> {
  const { data } = usePoses();
  return useMemo(() => new Map((data ?? []).map((p) => [p.id, p])), [data]);
}
