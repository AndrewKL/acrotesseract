import { useState } from 'react';
import { Link } from 'react-router';
import { usePoses, usePosesById, useTransitions } from '../api/hooks';
import { Loading, PoseList, QueryError, SearchInput, Section, TransitionList, usePageTitle } from '../components/ui';
import { filterByName } from '../lib/search';
import styles from './HomePage.module.css';

export function HomePage() {
  usePageTitle();
  const [query, setQuery] = useState('');
  const poses = usePoses();
  const transitions = useTransitions();
  const posesById = usePosesById();
  const ground = poses.data?.find((p) => p.name === 'Ground');

  return (
    <>
      <div className={styles.intro}>
        <h1>Acro Tesseract</h1>
        <p>
          Explore the world of acroyoga, transition by transition. Every pose is a point in the graph and every
          transition a path between two poses. Follow them and you'll see a world of movement flowing before you.
        </p>
        {ground && (
          <p>
            New here? Start from <Link to={`/poses/${ground.id}`}>Ground</Link> or explore the{' '}
            <Link to="/graph">graph</Link>.
          </p>
        )}
      </div>

      <SearchInput label="Search poses and transitions" value={query} onChange={setQuery} />

      <Section title="Poses">
        {poses.isPending ? (
          <Loading />
        ) : poses.isError ? (
          <QueryError error={poses.error} what="Poses" />
        ) : (
          <PoseList poses={filterByName(poses.data, query)} empty="No poses match." />
        )}
      </Section>

      <Section title="Transitions">
        {transitions.isPending ? (
          <Loading />
        ) : transitions.isError ? (
          <QueryError error={transitions.error} what="Transitions" />
        ) : (
          <TransitionList
            transitions={filterByName(transitions.data, query)}
            posesById={posesById}
            empty="No transitions match."
          />
        )}
      </Section>
    </>
  );
}
