import { useEffect, type ReactNode } from 'react';
import { Link } from 'react-router';
import ReactMarkdown from 'react-markdown';
import rehypeSanitize from 'rehype-sanitize';
import remarkGfm from 'remark-gfm';
import { ApiError } from '../api/client';
import type { Pose, Transition } from '../api/types';
import { youtubeEmbedUrl } from '../lib/youtube';
import styles from './ui.module.css';

/** Sets the browser tab title to "<title> · Acro Tesseract". */
export function usePageTitle(title?: string) {
  useEffect(() => {
    document.title = title ? `${title} · Acro Tesseract` : 'Acro Tesseract';
  }, [title]);
}

export function PageHeader(props: { eyebrow?: string; title: string; actions?: ReactNode }) {
  return (
    <div className={styles.pageHeader}>
      <h1>
        {props.eyebrow && <span className={styles.eyebrow}>{props.eyebrow}</span>}
        {props.title}
      </h1>
      {props.actions && <div className={styles.actions}>{props.actions}</div>}
    </div>
  );
}

export function ButtonLink(props: { to: string; children: ReactNode }) {
  return (
    <Link to={props.to} className={styles.button}>
      {props.children}
    </Link>
  );
}

export function Section(props: { title: string; children: ReactNode }) {
  return (
    <section className={styles.section} aria-label={props.title}>
      <h2>{props.title}</h2>
      {props.children}
    </section>
  );
}

export function Markdown(props: { children?: string }) {
  if (!props.children) return null;
  return (
    <div className={styles.markdown}>
      <ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeSanitize]}>
        {props.children}
      </ReactMarkdown>
    </div>
  );
}

export function YouTubeEmbed(props: { url?: string; title: string }) {
  const src = props.url ? youtubeEmbedUrl(props.url) : null;
  if (!src) return null;
  return (
    <div className={styles.video}>
      <iframe
        src={src}
        title={props.title}
        loading="lazy"
        allow="encrypted-media; picture-in-picture"
        allowFullScreen
      />
    </div>
  );
}

export function PoseList(props: { poses: Pose[]; empty?: string; meta?: (pose: Pose) => ReactNode }) {
  if (props.poses.length === 0) return <p className={styles.empty}>{props.empty ?? 'No poses.'}</p>;
  return (
    <ul className={styles.list}>
      {props.poses.map((pose) => (
        <li key={pose.id}>
          <Link to={`/poses/${pose.id}`}>{pose.name}</Link>
          {props.meta && <span className={styles.meta}>{props.meta(pose)}</span>}
        </li>
      ))}
    </ul>
  );
}

/** Transitions with where each one goes: "name — From → To". */
export function TransitionList(props: {
  transitions: Transition[];
  posesById: Map<string, Pose>;
  empty?: string;
}) {
  if (props.transitions.length === 0) {
    return <p className={styles.empty}>{props.empty ?? 'No transitions.'}</p>;
  }
  const poseLink = (id: string) => {
    const pose = props.posesById.get(id);
    return pose ? <Link to={`/poses/${id}`}>{pose.name}</Link> : '…';
  };
  return (
    <ul className={styles.list}>
      {props.transitions.map((t) => (
        <li key={t.id}>
          <Link to={`/transitions/${t.id}`}>{t.name}</Link>
          <span className={styles.meta}>
            {poseLink(t.poseFrom)} → {poseLink(t.poseTo)}
          </span>
        </li>
      ))}
    </ul>
  );
}

export function FromTo(props: { from: Pose; to: Pose }) {
  return (
    <div className={styles.fromTo} aria-label="From and to">
      <Link to={`/poses/${props.from.id}`} className={styles.chip}>
        {props.from.name}
      </Link>
      <span className={styles.arrow} aria-hidden="true">
        →
      </span>
      <Link to={`/poses/${props.to.id}`} className={styles.chip}>
        {props.to.name}
      </Link>
    </div>
  );
}

export function PoseImage(props: { pose: Pose }) {
  if (!props.pose.imageUrl) return null;
  return <img src={props.pose.imageUrl} alt={props.pose.name} className={styles.poseImage} />;
}

export function SearchInput(props: { value: string; onChange: (value: string) => void; label: string }) {
  return (
    <input
      type="search"
      className={styles.search}
      placeholder="Search poses and transitions"
      aria-label={props.label}
      autoComplete="off"
      value={props.value}
      onChange={(e) => props.onChange(e.target.value)}
    />
  );
}

export function Loading() {
  return (
    <p className={styles.state} role="status">
      Loading…
    </p>
  );
}

export function NotFound(props: { what?: string }) {
  usePageTitle('Not found');
  return (
    <div className={styles.state}>
      <h1>Not found</h1>
      <p>
        {props.what ?? 'This page'} doesn't exist. <Link to="/">Go home</Link>
      </p>
    </div>
  );
}

/** 404/400 from the API render as not found; anything else as an error. */
export function QueryError(props: { error: unknown; what: string }) {
  if (props.error instanceof ApiError && props.error.isNotFound) return <NotFound what={props.what} />;
  const message = props.error instanceof Error ? props.error.message : String(props.error);
  return (
    <p className={styles.state} role="alert">
      Couldn't load {props.what.toLowerCase()}: {message}
    </p>
  );
}
