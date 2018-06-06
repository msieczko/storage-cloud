import * as React from 'react';
import * as ReactDOM from 'react-dom';
import Users from './users-list';
import '../i18n';
import { UserDto } from './dto/user-dto';

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<Users selectUser={(user: UserDto) => undefined} />, div);
});