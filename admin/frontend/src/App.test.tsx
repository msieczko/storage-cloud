import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { TranslatedApp } from './App';
import './i18n';
import { HttpService } from './common/http-service';

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(
    (
      <TranslatedApp
        httpService={new HttpService()}
        refreshAuth={() => null}
      />),
    div);
});
