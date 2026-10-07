import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import enCommon from './en/common.json';
import zhCNCommon from './zh-CN/common.json';

/** UI languages shipped in the first phase (docs/00 §5.5); default zh-CN, fallback en. */
export const SUPPORTED_LANGUAGES = ['zh-CN', 'en'] as const;
export type SupportedLanguage = (typeof SUPPORTED_LANGUAGES)[number];

// Keys follow `feature.component.text`; one namespace per feature, `common` for the shell.
void i18n.use(initReactI18next).init({
  resources: {
    'zh-CN': { common: zhCNCommon },
    en: { common: enCommon },
  },
  lng: 'zh-CN',
  fallbackLng: 'en',
  defaultNS: 'common',
  ns: ['common'],
  interpolation: { escapeValue: false },
  initAsync: false,
});

export default i18n;
