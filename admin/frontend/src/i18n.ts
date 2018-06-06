import * as i18next from 'i18next';
import { reactI18nextModule } from 'react-i18next';
import * as Backend from 'i18next-xhr-backend';

const instance = i18next
  .use(Backend)
  .use(reactI18nextModule)
  .init({
    fallbackLng: 'en',

    ns: ['translations'],
    defaultNS: 'translations',

    debug: true,

    interpolation: {
      escapeValue: false,
    },

    react: {
      wait: true
    }
  });

export default instance;
