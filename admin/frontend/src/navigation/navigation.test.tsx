import * as React from 'react';
import * as ReactDOM from 'react-dom';
import Navigation from './navigation';
import '../i18n';
import { Tabs } from '../common/Tabs';

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(
    <Navigation
      upd={(() => null)}
      onSwitchTab={(f: Function) => f}
      currentTab={Tabs.Files}
      chosenPath={[]}
      chosenUser={null}
      selectPath={(path: String[], callback: Function) => undefined}
    />,
    div);
});
