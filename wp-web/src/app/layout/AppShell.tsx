import { Layout, Select, Space, Typography } from 'antd';
import { useTranslation } from 'react-i18next';
import { Outlet } from 'react-router-dom';

import { SUPPORTED_LANGUAGES, type SupportedLanguage } from '@/i18n';

const { Header, Content } = Layout;

/** Top bar + content area shared by every page (docs/03 §1). */
export function AppShell() {
  const { t, i18n } = useTranslation();

  return (
    <Layout className="app-shell">
      <Header className="app-shell__header">
        <Typography.Text className="app-shell__title">{t('app.shell.title')}</Typography.Text>
        <Space>
          <Select<SupportedLanguage>
            aria-label={t('app.shell.language')}
            size="small"
            value={i18n.language as SupportedLanguage}
            onChange={(language) => void i18n.changeLanguage(language)}
            options={SUPPORTED_LANGUAGES.map((language) => ({
              value: language,
              label: t(`app.shell.languages.${language}`),
            }))}
          />
        </Space>
      </Header>
      <Content className="app-shell__content">
        <Outlet />
      </Content>
    </Layout>
  );
}
