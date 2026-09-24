import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import App from './app';

describe('App', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('should have a title', () => {
    vi.stubGlobal('fetch', vi.fn(() => new Promise(() => undefined)));
    render(<App />);
    expect(screen.getByText('Acro Tesseract')).toBeInTheDocument();
  });

  it('should show the API status', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => Response.json({ status: 'ok', stage: 'local' })),
    );
    render(<App />);
    expect(await screen.findByText('API: ok (local)')).toBeInTheDocument();
  });
});
