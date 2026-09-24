import { Link, useNavigate, useSearchParams } from 'react-router';
import { usePoses, usePosesById, useTransitions } from '../api/hooks';
import { PoseGraph } from '../components/PoseGraph';
import { Loading, QueryError, usePageTitle } from '../components/ui';
import styles from './GraphPage.module.css';

export function GraphPage() {
  usePageTitle('Graph');
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const focusPoseId = params.get('focusPose') ?? undefined;
  const poses = usePoses();
  const transitions = useTransitions();
  const focusPose = usePosesById().get(focusPoseId ?? '');

  if (poses.isPending || transitions.isPending) return <Loading />;
  if (poses.isError) return <QueryError error={poses.error} what="Poses" />;
  if (transitions.isError) return <QueryError error={transitions.error} what="Transitions" />;

  return (
    <div className={styles.page}>
      <div className={styles.overlay}>
        {focusPose ? (
          <>
            Showing <strong>{focusPose.name}</strong> and its neighbors. <Link to="/graph">Show all</Link>
          </>
        ) : (
          <>
            <strong>{poses.data.length}</strong> poses, <strong>{transitions.data.length}</strong> transitions. Click a
            pose or transition to open it; drag to rearrange.
          </>
        )}
      </div>
      <PoseGraph
        poses={poses.data}
        transitions={transitions.data}
        focusPoseId={focusPoseId}
        onSelectPose={(id) => navigate(`/poses/${id}`)}
        onSelectTransition={(id) => navigate(`/transitions/${id}`)}
      />
    </div>
  );
}
