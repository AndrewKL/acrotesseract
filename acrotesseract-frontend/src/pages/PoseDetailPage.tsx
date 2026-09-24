import { useParams } from 'react-router';
import { usePose, usePosesById } from '../api/hooks';
import {
  ButtonLink,
  Loading,
  Markdown,
  PageHeader,
  PoseImage,
  QueryError,
  Section,
  TransitionList,
  usePageTitle,
} from '../components/ui';

export function PoseDetailPage() {
  const { poseId = '' } = useParams();
  const query = usePose(poseId);
  const posesById = usePosesById();
  usePageTitle(query.data?.pose.name);

  if (query.isPending) return <Loading />;
  if (query.isError) return <QueryError error={query.error} what="This pose" />;

  const { pose, transitionsFrom, transitionsTo } = query.data;
  return (
    <article>
      <PageHeader
        eyebrow="Pose"
        title={pose.name}
        actions={<ButtonLink to={`/graph?focusPose=${pose.id}`}>View in graph</ButtonLink>}
      />
      <PoseImage pose={pose} />
      <Markdown>{pose.descriptionMd}</Markdown>

      <Section title="Transitions from this pose">
        <TransitionList transitions={transitionsFrom} posesById={posesById} empty="No transitions leave this pose yet." />
      </Section>
      <Section title="Transitions to this pose">
        <TransitionList transitions={transitionsTo} posesById={posesById} empty="No transitions arrive at this pose yet." />
      </Section>
    </article>
  );
}
