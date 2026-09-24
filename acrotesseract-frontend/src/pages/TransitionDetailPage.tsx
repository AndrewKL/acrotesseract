import { useParams } from 'react-router';
import { useTransition } from '../api/hooks';
import {
  ButtonLink,
  FromTo,
  Loading,
  Markdown,
  PageHeader,
  QueryError,
  YouTubeEmbed,
  usePageTitle,
} from '../components/ui';

export function TransitionDetailPage() {
  const { transitionId = '' } = useParams();
  const query = useTransition(transitionId);
  usePageTitle(query.data?.transition.name);

  if (query.isPending) return <Loading />;
  if (query.isError) return <QueryError error={query.error} what="This transition" />;

  const { transition, poseFrom, poseTo } = query.data;
  return (
    <article>
      <PageHeader
        eyebrow="Transition"
        title={transition.name}
        actions={<ButtonLink to={`/graph?focusPose=${poseFrom.id}`}>View in graph</ButtonLink>}
      />
      <FromTo from={poseFrom} to={poseTo} />
      <YouTubeEmbed url={transition.youtubeUrl} title={transition.name} />
      <Markdown>{transition.descriptionMd}</Markdown>
    </article>
  );
}
