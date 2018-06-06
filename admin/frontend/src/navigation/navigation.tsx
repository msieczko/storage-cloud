import * as React from 'react';
import { translate } from 'react-i18next';
import { Tabs } from '../common/Tabs';
import { withAppHoc, WithAppProps } from '../common/app-hoc';
import { ApplicationProps } from '../App';
import { UserDto } from '../users/dto/user-dto';

interface NavigationProps {
  onSwitchTab: Function;
  currentTab: Tabs;
  upd: Function;
  chosenUser: UserDto | null;
  chosenPath: String[];
  selectPath: (path: String[], callback: Function) => void;
}

class Navigation extends React.Component<ApplicationProps & WithAppProps & NavigationProps> {

  constructor(props: ApplicationProps & WithAppProps & NavigationProps) {
    super(props);
    this.logout = this.logout.bind(this);
    this.subDirOnClick = this.subDirOnClick.bind(this);
    this.usernameOnClick = this.usernameOnClick.bind(this);
    this.closeButtonOnClick = this.closeButtonOnClick.bind(this);
  }

  changeLanguage(lang: string) {
    this.props.i18n.changeLanguage(lang);
  }

  logout() {
    this.props.httpService.get('/api/logout').then(() => {
        this.props.httpService.isSignedIn = false;
        this.props.upd();
      }
    );
  }

  subDirOnClick(index: number) {
    this.props.selectPath(this.props.chosenPath.slice(0, index + 1), () => undefined);
  }

  usernameOnClick() {
    console.log(this.props);
    this.props.selectPath([], () => undefined);
  }

  closeButtonOnClick() {
    this.props.onSwitchTab(Tabs.Users, true);
  }

  render() {
    const {t} = this.props;

    const NavigationLink = (props: {
      title: string,
      onClick: (event: React.MouseEvent<HTMLAnchorElement>) => void,
      isActive: boolean
    }) => {
      return (
        <li className={'nav-item ' + (props.isActive ? 'active' : '')}>
          <a
            className="nav-link"
            href="#"
            onClick={props.onClick}
          >
            {t(props.title)}
          </a>
        </li>);
    };

    const CloseButton = () => {
      return (
        <button type="button" className="close ml-2" aria-label="Close" onClick={this.closeButtonOnClick}>
          <span aria-hidden="true">&times;</span>
        </button>
      );
    };

    const Breadcrumb = () => {
      const className = 'breadcrumb m-1 px-2 py-1';
      if (this.props.chosenPath.length > 0) {
        const length = this.props.chosenPath.length;
        return (
          <ol className={className}>
            <li className="breadcrumb-item" onClick={this.usernameOnClick}><a href="#">
              {this.props.chosenUser && this.props.chosenUser.username}
            </a></li>
            {this.props.chosenPath.map((subDir, i) => {
              if (i === length - 1) {
                return (<li key={i} className="breadcrumb-item" aria-current="page">{subDir}</li>);
              }
              return (<li key={i} className="breadcrumb-item" onClick={() => this.subDirOnClick(i)}><a href="#">
                {subDir}
              </a></li>);
            })}
            <CloseButton/>
          </ol>
        );
      } else {
        return (
          <ol className={className}>
            <li className="breadcrumb-item" aria-current="page">
              {this.props.chosenUser && this.props.chosenUser.username}
            </li>
            <CloseButton/>
          </ol>
        );
      }
    };

    return (
      <nav className="navbar navbar-expand-lg navbar-light bg-light justify-content-center">
        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav">
            <NavigationLink
              onClick={this.props.onSwitchTab.bind(this, Tabs.Users, false)}
              isActive={this.props.currentTab === Tabs.Users}
              title="MENU.USERS"
            />
            <NavigationLink
              onClick={this.props.onSwitchTab.bind(this, Tabs.Files, false)}
              isActive={this.props.currentTab === Tabs.Files}
              title="MENU.FILES"
            />
            {this.props.chosenUser &&
            <nav aria-label="breadcrumb">
              <Breadcrumb/>
            </nav>
            }
          </ul>
        </div>
        <button className="btn btn-outline-info mr-1" onClick={this.changeLanguage.bind(this, 'en')}>EN</button>
        <button className="btn btn-outline-info" onClick={this.changeLanguage.bind(this, 'pl')}>PL</button>
        <button
          className="btn btn-outline-secondary ml-4"
          onClick={this.logout}
        >
          {t('MENU.BUTTONS.LOGOUT')}
        </button>
      </nav>
    )
      ;
  }
}

export default translate()(withAppHoc((Navigation)));