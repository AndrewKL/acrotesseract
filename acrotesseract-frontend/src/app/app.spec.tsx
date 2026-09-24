import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { renderApp, stubApi } from '../test/render';
import * as f from '../test/fixtures';

vi.mock('../components/PoseGraph', () => ({
  PoseGraph: (props: { poses: unknown[]; transitions: unknown[]; focusPoseId?: string }) => (
    <div data-testid="pose-graph">
      {props.poses.length} nodes, {props.transitions.length} edges, focus {props.focusPoseId ?? 'none'}
    </div>
  ),
}));

const listRoutes = { '/api/poses': f.poses, '/api/transitions': f.transitions };

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('layout', () => {
  it('shows the brand and main navigation on every page', async () => {
    stubApi(listRoutes);
    renderApp('/poses');
    const nav = screen.getByRole('navigation', { name: 'Main' });
    expect(within(nav).getByRole('link', { name: 'Poses' })).toHaveAttribute('href', '/poses');
    expect(within(nav).getByRole('link', { name: 'Transitions' })).toHaveAttribute('href', '/transitions');
    expect(within(nav).getByRole('link', { name: 'Graph' })).toHaveAttribute('href', '/graph');
    expect(screen.getByRole('link', { name: 'Acro Tesseract' })).toHaveAttribute('href', '/');
  });

  it('shows a not-found page for unknown URLs', async () => {
    stubApi(listRoutes);
    renderApp('/nope');
    expect(screen.getByRole('heading', { name: 'Not found' })).toBeInTheDocument();
  });
});

describe('home page', () => {
  it('lists poses and transitions and filters them as you type', async () => {
    stubApi(listRoutes);
    renderApp('/');
    const posesSection = await screen.findByRole('region', { name: 'Poses' });
    expect(await within(posesSection).findByRole('link', { name: 'Throne' })).toBeInTheDocument();
    expect(within(posesSection).getByRole('link', { name: 'Ground' })).toHaveAttribute('href', `/poses/${f.ground.id}`);
    // The intro points newcomers at the starting pose.
    expect(screen.getByText(/New here\?/)).toHaveTextContent('Start from Ground');

    await userEvent.type(screen.getByRole('searchbox'), 'thr');
    expect(within(posesSection).getByRole('link', { name: 'Throne' })).toBeInTheDocument();
    expect(within(posesSection).queryByRole('link', { name: 'Front Bird' })).not.toBeInTheDocument();
    const transitionsSection = screen.getByRole('region', { name: 'Transitions' });
    expect(within(transitionsSection).getByRole('link', { name: 'Front Bird to Throne' })).toBeInTheDocument();
    expect(within(transitionsSection).queryByRole('link', { name: 'Ground to Front Bird' })).not.toBeInTheDocument();
  });

  it('shows an error when the API is down', async () => {
    vi.stubGlobal('fetch', vi.fn(async () => Response.json({ error: 'boom' }, { status: 500 })));
    renderApp('/');
    expect(await screen.findAllByRole('alert')).toHaveLength(2);
  });
});

describe('pose list page', () => {
  it('lists poses with their transition counts', async () => {
    stubApi(listRoutes);
    renderApp('/poses');
    expect(await screen.findByRole('heading', { name: 'Poses' })).toBeInTheDocument();
    const bird = await screen.findByRole('link', { name: 'Front Bird' });
    expect(await within(bird.closest('li')!).findByText('1 out · 1 in')).toBeInTheDocument();
  });
});

describe('pose detail page', () => {
  it('shows the pose, its Markdown description and where each transition goes', async () => {
    stubApi({ ...listRoutes, [`/api/poses/${f.bird.id}`]: f.birdDetail });
    renderApp(`/poses/${f.bird.id}`);

    expect(await screen.findByRole('heading', { name: /Front Bird/ })).toBeInTheDocument();
    expect(screen.getByText('base').tagName).toBe('STRONG');
    expect(screen.getByRole('img', { name: 'Front Bird' })).toHaveAttribute('src', f.bird.imageUrl);
    expect(screen.getByRole('link', { name: 'View in graph' })).toHaveAttribute('href', `/graph?focusPose=${f.bird.id}`);

    const from = screen.getByRole('region', { name: 'Transitions from this pose' });
    expect(within(from).getByRole('link', { name: 'Front Bird to Throne' })).toHaveAttribute(
      'href',
      `/transitions/${f.birdToThrone.id}`,
    );
    expect(await within(from).findByRole('link', { name: 'Throne' })).toHaveAttribute('href', `/poses/${f.throne.id}`);

    const to = screen.getByRole('region', { name: 'Transitions to this pose' });
    expect(within(to).getByRole('link', { name: 'Ground to Front Bird' })).toBeInTheDocument();
    expect(document.title).toBe('Front Bird · Acro Tesseract');
  });

  it('shows not found for an unknown pose', async () => {
    stubApi(listRoutes);
    renderApp('/poses/99999999-9999-4999-8999-999999999999');
    expect(await screen.findByRole('heading', { name: 'Not found' })).toBeInTheDocument();
  });
});

describe('transition pages', () => {
  it('lists transitions with their from and to poses', async () => {
    stubApi(listRoutes);
    renderApp('/transitions');
    const row = (await screen.findByRole('link', { name: 'Ground to Front Bird' })).closest('li')!;
    expect(await within(row).findByRole('link', { name: 'Ground' })).toBeInTheDocument();
    expect(within(row).getByRole('link', { name: 'Front Bird' })).toBeInTheDocument();
  });

  it('shows the transition with its video and both poses', async () => {
    stubApi({ ...listRoutes, [`/api/transitions/${f.groundToBird.id}`]: f.groundToBirdDetail });
    renderApp(`/transitions/${f.groundToBird.id}`);

    expect(await screen.findByRole('heading', { name: /Ground to Front Bird/ })).toBeInTheDocument();
    expect(screen.getByTitle('Ground to Front Bird')).toHaveAttribute(
      'src',
      'https://www.youtube-nocookie.com/embed/g8OhDBRwhSw?start=403',
    );
    const fromTo = screen.getByLabelText('From and to');
    expect(within(fromTo).getByRole('link', { name: 'Ground' })).toHaveAttribute('href', `/poses/${f.ground.id}`);
    expect(within(fromTo).getByRole('link', { name: 'Front Bird' })).toHaveAttribute('href', `/poses/${f.bird.id}`);
    expect(screen.getByText('Standing entry.')).toBeInTheDocument();
  });
});

describe('graph page', () => {
  it('passes every pose and transition to the graph, with the focused pose', async () => {
    stubApi(listRoutes);
    renderApp(`/graph?focusPose=${f.bird.id}`);
    expect(await screen.findByTestId('pose-graph')).toHaveTextContent(`3 nodes, 2 edges, focus ${f.bird.id}`);
    expect(await screen.findByText('Front Bird')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Show all' })).toHaveAttribute('href', '/graph');
  });
});
