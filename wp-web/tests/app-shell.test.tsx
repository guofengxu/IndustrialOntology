import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

import { AppProviders } from '@/app/AppProviders';
import { AppRoutes, routerFuture } from '@/app/router';

function renderAt(path: string) {
  render(
    <AppProviders>
      <MemoryRouter initialEntries={[path]} future={routerFuture}>
        <AppRoutes />
      </MemoryRouter>
    </AppProviders>,
  );
}

describe('AppShell', () => {
  it('redirects the root path to the project list', async () => {
    renderAt('/');

    expect(await screen.findByRole('heading', { name: '项目' })).toBeInTheDocument();
    expect(screen.getByText('IndustrialOntology')).toBeInTheDocument();
  });

  it('shows a 404 result for unknown paths', async () => {
    renderAt('/no-such-page');

    expect(await screen.findByText('页面不存在。')).toBeInTheDocument();
  });
});
