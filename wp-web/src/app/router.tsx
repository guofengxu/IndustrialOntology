import { Navigate, useRoutes, type RouteObject } from 'react-router-dom';

import { AppShell } from '@/app/layout/AppShell';
import { NotFoundPage } from '@/app/layout/NotFoundPage';
import { PlaceholderPage } from '@/app/layout/PlaceholderPage';

/** Opt into the v7 behaviours now so the later react-router upgrade is a no-op. */
export const routerFuture = {
  v7_startTransition: true,
  v7_relativeSplatPath: true,
} as const;

/** Route table (docs/03 §2). Feature pages replace the placeholders task by task. */
export const routes: RouteObject[] = [
  {
    element: <AppShell />,
    children: [
      { index: true, element: <Navigate to="/projects" replace /> },
      { path: 'projects', element: <PlaceholderPage titleKey="app.nav.projects" task="P2-02" /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
];

/**
 * Renders {@link routes} inside whichever router the caller provides (BrowserRouter in the
 * app, MemoryRouter in tests). Server data is loaded through TanStack Query, so the
 * data-router APIs (loaders/actions) are intentionally not used.
 */
export function AppRoutes() {
  return useRoutes(routes);
}
