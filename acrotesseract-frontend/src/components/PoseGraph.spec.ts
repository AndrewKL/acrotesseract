import { describe, expect, it, vi } from 'vitest';

vi.mock('cytoscape', () => ({ default: { use: vi.fn() } }));
vi.mock('cytoscape-cola', () => ({ default: {} }));

import { toElements } from './PoseGraph';

describe('toElements', () => {
  it('maps poses to nodes and transitions to directed edges', () => {
    const elements = toElements(
      [
        { id: 'a', name: 'Ground' },
        { id: 'b', name: 'Front Bird' },
      ],
      [{ id: 't', name: 'Ground to Front Bird', poseFrom: 'a', poseTo: 'b' }],
    );
    expect(elements).toEqual([
      { group: 'nodes', data: { id: 'pose:a', poseId: 'a', name: 'Ground' } },
      { group: 'nodes', data: { id: 'pose:b', poseId: 'b', name: 'Front Bird' } },
      {
        group: 'edges',
        data: { id: 'transition:t', transitionId: 't', name: 'Ground to Front Bird', source: 'pose:a', target: 'pose:b' },
      },
    ]);
  });
});
