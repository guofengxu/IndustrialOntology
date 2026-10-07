import { Typography } from 'antd';
import { useTranslation } from 'react-i18next';

interface PlaceholderPageProps {
  titleKey: string;
  /** docs/05 task that delivers the real page. */
  task: string;
}

export function PlaceholderPage({ titleKey, task }: PlaceholderPageProps) {
  const { t } = useTranslation();

  return (
    <>
      <Typography.Title level={3}>{t(titleKey)}</Typography.Title>
      <Typography.Paragraph type="secondary">
        {t('app.placeholder.notImplemented', { task })}
      </Typography.Paragraph>
    </>
  );
}
