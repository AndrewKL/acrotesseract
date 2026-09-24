import cytoscape, { type Core, type ElementDefinition, type LayoutOptions } from 'cytoscape';
import cola from 'cytoscape-cola';
import { useEffect, useRef } from 'react';
import type { Pose, Transition } from '../api/types';
import styles from './PoseGraph.module.css';

cytoscape.use(cola);

/** Poses become nodes and transitions directed edges. Ids are prefixed so a pose and a transition can't collide. */
export function toElements(poses: Pose[], transitions: Transition[]): ElementDefinition[] {
  return [
    ...poses.map((p) => ({ group: 'nodes' as const, data: { id: `pose:${p.id}`, poseId: p.id, name: p.name } })),
    ...transitions.map((t) => ({
      group: 'edges' as const,
      data: {
        id: `transition:${t.id}`,
        transitionId: t.id,
        name: t.name,
        source: `pose:${t.poseFrom}`,
        target: `pose:${t.poseTo}`,
      },
    })),
  ];
}

function cssVar(name: string, fallback: string): string {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim() || fallback;
}

// Cola is force-directed; edgeLength is the length it tries to reach for every edge.
const layoutOptions = (fit: boolean) =>
  ({ name: 'cola', fit, padding: 40, edgeLength: 150, animate: true, maxSimulationTime: 2500 }) as LayoutOptions;

export interface PoseGraphProps {
  poses: Pose[];
  transitions: Transition[];
  focusPoseId?: string;
  onSelectPose: (poseId: string) => void;
  onSelectTransition: (transitionId: string) => void;
}

export function PoseGraph(props: PoseGraphProps) {
  const container = useRef<HTMLDivElement>(null);
  const handlers = useRef(props);
  handlers.current = props;

  useEffect(() => {
    if (!container.current) return;
    const fg = cssVar('--fg', '#1f2328');
    const node = cssVar('--graph-node', '#57606a');
    const edge = cssVar('--graph-edge', '#afb8c1');
    const focus = cssVar('--graph-focus', '#0969da');

    const cy: Core = cytoscape({
      container: container.current,
      elements: toElements(props.poses, props.transitions),
      wheelSensitivity: 0.2,
      style: [
        {
          selector: 'node',
          style: {
            'background-color': node,
            label: 'data(name)',
            color: fg,
            'font-size': 12,
            'text-valign': 'bottom',
            'text-margin-y': 4,
            width: 18,
            height: 18,
          },
        },
        {
          selector: 'edge',
          style: {
            width: 2,
            'line-color': edge,
            'target-arrow-color': edge,
            'target-arrow-shape': 'triangle',
            'curve-style': 'bezier',
          },
        },
        { selector: 'edge.hover', style: { label: 'data(name)', 'font-size': 11, color: fg, 'text-rotation': 'autorotate' } },
        { selector: '.focus', style: { 'background-color': focus, 'line-color': focus, 'target-arrow-color': focus } },
        { selector: 'node.focus-root', style: { width: 26, height: 26, 'font-weight': 'bold' } },
        { selector: '.faded', style: { opacity: 0.25 } },
      ],
    });

    const focusNode = props.focusPoseId ? cy.getElementById(`pose:${props.focusPoseId}`) : undefined;
    const layout = cy.layout(layoutOptions(true));
    if (focusNode && focusNode.nonempty()) {
      const hood = focusNode.closedNeighborhood();
      hood.addClass('focus');
      focusNode.addClass('focus-root');
      cy.elements().not(hood).addClass('faded');
      layout.one('layoutstop', () => cy.animate({ fit: { eles: hood, padding: 60 } }, { duration: 300 }));
    }
    layout.run();

    // Re-flow once a drag ends, not on every drag event.
    cy.on('dragfree', 'node', () => cy.layout(layoutOptions(false)).run());
    cy.on('tap', 'node', (e) => handlers.current.onSelectPose(e.target.data('poseId')));
    cy.on('tap', 'edge', (e) => handlers.current.onSelectTransition(e.target.data('transitionId')));
    cy.on('mouseover', 'edge', (e) => e.target.addClass('hover'));
    cy.on('mouseout', 'edge', (e) => e.target.removeClass('hover'));

    return () => cy.destroy();
  }, [props.poses, props.transitions, props.focusPoseId]);

  return <div ref={container} className={styles.graph} data-testid="pose-graph" />;
}
