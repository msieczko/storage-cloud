import * as React from 'react';
import { i18n } from 'i18next';
import { translate } from 'react-i18next';

import Files from './files/files-list';
import Users from './users/users-list';
import './App.css';
import Navigation from './navigation/navigation';
import { Tabs } from './common/Tabs';
import { default as withAppHoc, WithAppProps } from './common/app-hoc';
import { withAuthHoc, WithAuthProps } from './common/auth-hoc';
import { UserDto } from './users/dto/user-dto';

export interface ApplicationProps {
  t: Function;
  i18n: i18n;
}

interface AppState {
  currentTab: Tabs;
  chosenUser: UserDto | null;
  chosenPath: String[];
}

class App extends React.Component<ApplicationProps & WithAppProps & WithAuthProps, AppState> {

  constructor(props: ApplicationProps & WithAppProps & WithAuthProps) {
    super(props);
    this.state = {
      currentTab: Tabs.Users,
      chosenUser: null,
      chosenPath: []
    };
    this.selectUser = this.selectUser.bind(this);
    this.selectPath = this.selectPath.bind(this);
    this.switchTab = this.switchTab.bind(this);
  }

  selectUser(user: UserDto) {
    this.setState({chosenUser: user, chosenPath: [], currentTab: Tabs.Files});
  }

  selectPath(path: String[], callback: Function) {
    this.setState({chosenPath: path, currentTab: Tabs.Files}, () => {
      console.log('selectPath().callback:');
      callback();
    });
  }

  switchTab(nextTab: Tabs, close: boolean) {
    if (close) {
      this.setState({currentTab: nextTab, chosenPath: [], chosenUser: null});
      return;
    }
    this.setState({currentTab: nextTab});
  }

  render() {
    return (
      <div>
        <div className="container">
          <header className="col header text-center">
            <h1 className="m-0 py-3 font-weight-bold">Admin Panel</h1>
          </header>
          <Navigation
            onSwitchTab={this.switchTab}
            currentTab={this.state.currentTab}
            upd={this.props.refreshAuth}
            chosenPath={this.state.chosenPath}
            chosenUser={this.state.chosenUser}
            selectPath={this.selectPath}
          />
        </div>
        <div className="container">
          <div className="col">
            {this.state.currentTab === Tabs.Users && <Users selectUser={this.selectUser}/>}
            {this.state.currentTab === Tabs.Files &&
            <Files
              chosenUser={this.state.chosenUser}
              chosenPath={this.state.chosenPath}
              selectPath={this.selectPath}
            />}
          </div>
        </div>
      </div>
    );
  }
}

// export default App;
// export { App };
export const TranslatedApp = translate()(App);
export default translate()(withAppHoc(withAuthHoc(App)));
