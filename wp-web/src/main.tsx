import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';

import { AppProviders } from '@/app/AppProviders';
import { AppRoutes, routerFuture } from '@/app/router';
import '@/i18n';
import '@/styles/global.css';

const container = document.getElementById('root');
if (!container) {
  throw new Error('Root element #root not found');
}

createRoot(container).render(
  <StrictMode>
    <AppProviders>
      <BrowserRouter future={routerFuture}>
        <AppRoutes />
      </BrowserRouter>
    </AppProviders>
  </StrictMode>,
);
