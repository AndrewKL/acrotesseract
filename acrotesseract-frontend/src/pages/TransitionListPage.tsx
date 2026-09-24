import { usePosesById, useTransitions } from '../api/hooks';
import { Loading, PageHeader, QueryError, TransitionList, usePageTitle } from '../components/ui';

export function TransitionListPage() {
  usePageTitle('Transitions');
  const transitions = useTransitions();
  const posesById = usePosesById();

  return (
    <>
      <PageHeader title="Transitions" />
      {transitions.isPending ? (
        <Loading />
      ) : transitions.isError ? (
        <QueryError error={transitions.error} what="Transitions" />
      ) : (
        <TransitionList transitions={transitions.data} posesById={posesById} />
      )}
    </>
  );
}
