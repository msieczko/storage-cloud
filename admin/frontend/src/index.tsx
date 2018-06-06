import * as React from 'react';
import * as ReactDOM from 'react-dom';
import registerServiceWorker from './registerServiceWorker';
import I18nextProvider from 'react-i18next/src/I18nextProvider';
import App from './App';
import './index.css';
import i18n from './i18n';
import { HttpService } from './common/http-service';
import { UserDto } from './users/dto/user-dto';
import { AppContext } from './common/app-hoc';

export interface AppContextData {
  httpsrv: HttpService;
  currentUser?: UserDto; // TODO here or in hoc props?
}

let appContextData: AppContextData = {httpsrv: new HttpService()};

ReactDOM.render(
  <I18nextProvider i18n={i18n}>
    <AppContext.Provider value={appContextData}>
      <App/>
    </AppContext.Provider>
  </I18nextProvider>,
  document.getElementById('root') as HTMLElement
);
registerServiceWorker();