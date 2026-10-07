import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ConfigProvider } from 'antd';
import enUS from 'antd/locale/en_US';
import zhCN from 'antd/locale/zh_CN';
import { useState, type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';

/**
 * Cross-cutting providers: TanStack Query for server state (docs/00 §5.2) and the
 * Ant Design locale, which follows the UI language chosen through i18next.
 */
export function AppProviders({ children }: { children: ReactNode }) {
  const { i18n } = useTranslation();
  // One client per mounted tree, so tests never share cached server state.
  const [queryClient] = useState(
    () => new QueryClient({ defaultOptions: { queries: { staleTime: 30_000, retry: 1 } } }),
  );

  return (
    <QueryClientProvider client={queryClient}>
      <ConfigProvider locale={i18n.language === 'en' ? enUS : zhCN}>{children}</ConfigProvider>
    </QueryClientProvider>
  );
}
