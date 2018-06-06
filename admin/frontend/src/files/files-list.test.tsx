import * as React from 'react';
import * as ReactDOM from 'react-dom';
import FilesList from './files-list';
import '../i18n';

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(
    (
      <FilesList
        selectPath={(path: String[], callback: Function) => undefined}
        chosenUser={null}
        chosenPath={[]}
      />
    ),
    div);
});
