import { Result } from 'antd';
import { useTranslation } from 'react-i18next';

export function NotFoundPage() {
  const { t } = useTranslation();

  return <Result status="404" title="404" subTitle={t('app.errors.notFound')} />;
}
