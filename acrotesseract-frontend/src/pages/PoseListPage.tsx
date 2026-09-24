import { useMemo } from 'react';
import { usePoses, useTransitions } from '../api/hooks';
import { Loading, PageHeader, PoseList, QueryError, usePageTitle } from '../components/ui';

export function PoseListPage() {
  usePageTitle('Poses');
  const poses = usePoses();
  const transitions = useTransitions();

  const counts = useMemo(() => {
    const out = new Map<string, number>();
    const inc = new Map<string, number>();
    for (const t of transitions.data ?? []) {
      out.set(t.poseFrom, (out.get(t.poseFrom) ?? 0) + 1);
      inc.set(t.poseTo, (inc.get(t.poseTo) ?? 0) + 1);
    }
    return { out, inc };
  }, [transitions.data]);

  return (
    <>
      <PageHeader title="Poses" />
      {poses.isPending ? (
        <Loading />
      ) : poses.isError ? (
        <QueryError error={poses.error} what="Poses" />
      ) : (
        <PoseList
          poses={poses.data}
          meta={
            transitions.data
              ? (p) => `${counts.out.get(p.id) ?? 0} out · ${counts.inc.get(p.id) ?? 0} in`
              : undefined
          }
        />
      )}
    </>
  );
}
