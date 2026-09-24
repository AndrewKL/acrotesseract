import type { Pose, PoseDetail, Transition, TransitionDetail } from './types';

export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }

  /** 404, or 400 for a malformed id: either way, there's nothing at this URL. */
  get isNotFound(): boolean {
    return this.status === 404 || this.status === 400;
  }
}

async function getJson<T>(path: string): Promise<T> {
  const res = await fetch(path, { headers: { Accept: 'application/json' } });
  if (!res.ok) {
    let message = `${res.status} ${res.statusText}`.trim();
    try {
      const body = (await res.json()) as { error?: string };
      if (body.error) message = body.error;
    } catch {
      // Not JSON; keep the status text.
    }
    throw new ApiError(res.status, message);
  }
  return (await res.json()) as T;
}

export const api = {
  listPoses: () => getJson<Pose[]>('/api/poses'),
  getPose: (id: string) => getJson<PoseDetail>(`/api/poses/${encodeURIComponent(id)}`),
  listTransitions: () => getJson<Transition[]>('/api/transitions'),
  getTransition: (id: string) =>
    getJson<TransitionDetail>(`/api/transitions/${encodeURIComponent(id)}`),
};
