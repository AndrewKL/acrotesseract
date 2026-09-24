export interface Health {
  status: string;
  stage: string;
}

/** Calls the backend's health check. In dev, Vite proxies /api to the local backend. */
export async function fetchHealth(): Promise<Health> {
  const res = await fetch('/api/health');
  if (!res.ok) {
    throw new Error(`GET /api/health failed: ${res.status}`);
  }
  return res.json();
}
