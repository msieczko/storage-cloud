import * as React from 'react';
import { translate } from 'react-i18next';
import { UserDto } from './dto/user-dto';
import NewUserModal from './new-user-modal';
import withAppHoc, { WithAppProps } from '../common/app-hoc';
import { HttpService } from '../common/http-service';
import { ApplicationProps } from '../App';
import { NewUserDto } from './dto/new-user-dto';
import { Utils } from '../common/utils';

interface UsersListState {
  fetchedUsers: UserDto[];
  filteredUsers: UserDto[];
  errorInfo: string;
  filters: {
    usernameFilter: string,
    fullNameFilter: string,
  };
}

class UsersList extends React.Component<ApplicationProps & WithAppProps & { selectUser: (user: UserDto) => void },
  UsersListState> {
  private httpService: HttpService;

  constructor(props: ApplicationProps & WithAppProps & { selectUser: (user: UserDto) => void }) {
    super(props);
    this.httpService = props.httpService;

    this.state = {
      filteredUsers: [],
      fetchedUsers: [],
      errorInfo: '',
      filters: {
        usernameFilter: '',
        fullNameFilter: '',
      }
    };

    this.handleFilterValueChange = this.handleFilterValueChange.bind(this);
    this.handleUserCreation = this.handleUserCreation.bind(this);
    this.fetchList = this.fetchList.bind(this);
  }

  fetchList() {
    this.httpService.get('api/users')
      .then(response => {
          if (response.ok) {
            response.json()
              .then(data => this.setState({
                filteredUsers: data,
                fetchedUsers: data
              })).catch(this.setErrorInfo());
          }
        }
      ).catch(this.setErrorInfo());
  }

  componentDidMount() {
    this.fetchList();
  }

  setErrorInfo() {
    return (message: Response) => {
      this.setState({errorInfo: message.toString()});
    };
  }

  refreshFilter() {
    let filteredUsers = this.state.fetchedUsers.filter(value => {
      let show: boolean = true;
      if (this.state.filters.usernameFilter !== '') {
        show = value.username.toLowerCase().indexOf(this.state.filters.usernameFilter.toLowerCase()) >= 0;
      }
      if (this.state.filters.fullNameFilter !== '' && show) {
        show = (value.firstName.toLowerCase() + ' ' + value.lastName.toLowerCase())
          .indexOf(this.state.filters.fullNameFilter.toLowerCase()) >= 0;
      }
      return show;
    });

    this.setState({filteredUsers: filteredUsers});
  }

  handleFilterValueChange(e: React.ChangeEvent<HTMLInputElement>) {
    const {name, value} = e.target;

    this.setState(
      {filters: {...this.state.filters, [name]: value}},
      () => {
        this.refreshFilter();
      });
  }

  handleUserCreation(newUser: NewUserDto) {
    this.httpService.post('api/users', newUser).then((response) => {
      if (response instanceof Response) {
        response.text().then((text) => {
          console.log(text);
          if (text === 'OK') {
            this.fetchList();
          } else {
            this.setState({errorInfo: text});
          }
        }).catch(reason => this.setState({errorInfo: reason.toString()}));
      }
    }).catch(reason => this.setState({errorInfo: reason.toString()}));
  }

  render() {
    const {t} = this.props;

    const UserRow = (props: { userRow: UserDto }) => {
      return (
        <tr className="clickable-row" onClick={() => this.props.selectUser(props.userRow)}>
          <td>{props.userRow.username}</td>
          <td>{props.userRow.firstName} {props.userRow.lastName}</td>
          <td>{Utils.humanFileSize(props.userRow.usedSpace, true)}</td>
          <td>{Utils.humanFileSize(props.userRow.capacity, true)}</td>
        </tr>
      );
    };

    function User(props: { users: UserDto[] }) {
      return (
        <tbody>
        {props.users.map((user) =>
          <UserRow key={user.username} userRow={user}/>
        )}
        </tbody>
      );
    }

    return (
      <div>
        <div className="card row">
          <div className="card-body row">

            <div className="col">
              <div className="row">
                <h6 className="col-auto card-subtitle mt-2">
                  {t('COMMONS.FILTER')}
                </h6>
                <input
                  type="text"
                  name="usernameFilter"
                  className="col-3 form-control mr-3"
                  value={this.state.filters.usernameFilter}
                  onChange={this.handleFilterValueChange}
                  placeholder={t('USERS.FILTER.USERNAME')}
                />
                <input
                  type="text"
                  name="fullNameFilter"
                  className="col-3 form-control mr-3"
                  value={this.state.filters.fullNameFilter}
                  onChange={this.handleFilterValueChange}
                  placeholder={t('USERS.FILTER.FULL_NAME')}
                />
              </div>
            </div>

            <div className="col-2">

              <button
                type="button"
                className="btn float-right"
                data-toggle="modal"
                data-target="#newUserModal"
              >
                {t('USERS.ADD_BUTTON')}
              </button>
              <NewUserModal createUser={this.handleUserCreation}/>

            </div>

          </div>
        </div>
        {
          this.state.errorInfo !== '' &&
          <div className="row my-1 alert alert-danger" role="alert">
            {this.state.errorInfo}
          </div>
        }
        <div className="row">
          <table className="table table-hover table-bordered text-left">
            <thead className="thead-light">
            <tr>
              <th>{t('USERS.LIST_HEADER.USERNAME')}</th>
              <th>{t('USERS.LIST_HEADER.FULL_NAME')}</th>
              <th>{t('USERS.LIST_HEADER.USED_SPACE')}</th>
              <th>{t('USERS.LIST_HEADER.CAPACITY')}</th>
            </tr>
            </thead>
            <User users={this.state.filteredUsers}/>
          </table>
        </div>
      </div>
    );
  }
}

export default translate()(withAppHoc(UsersList));
