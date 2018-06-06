import * as React from 'react';
import { NewUserDto } from './dto/new-user-dto';
import { translate } from 'react-i18next';
import { ApplicationProps } from '../App';

interface NewUserModalState {
  newUser: NewUserDto;
  emptyFields: boolean;
  serverMessage: string | null;
}

interface NewUserModalProps {
  createUser: Function;
}

class NewUserModal extends React.Component<ApplicationProps & NewUserModalProps, NewUserModalState> {
  private emptyUser: NewUserDto = {firstName: '', lastName: '', username: '', password: ''};

  constructor(props: ApplicationProps & NewUserModalProps) {
    super(props);

    this.state = {
      newUser: {
        username: '',
        password: '',
        firstName: '',
        lastName: '',
      },
      emptyFields: true,
      serverMessage: null
    };
  }

  handleUserCreation() {
    console.log(this.state.newUser);
    this.props.createUser(this.state.newUser);
    this.clearModalForm();
  }

  validateUserPropState(value: string) {
    const length = value.length;
    if (length > 60 || length === 0) {
      this.setState({emptyFields: true});
    }
  }

  validateUserState() {
    this.setState({emptyFields: false});
    this.validateUserPropState(this.state.newUser.username);
    this.validateUserPropState(this.state.newUser.password);
    this.validateUserPropState(this.state.newUser.lastName);
    this.validateUserPropState(this.state.newUser.firstName);
  }

  clearModalForm() {
    const form = document.getElementsByClassName('new-user-form')[0] as HTMLFormElement;
    form.reset();
    this.setState({newUser: this.emptyUser, emptyFields: true});
  }

  render() {
    const {t} = this.props;
    return (
      <div id="newUserModal" className="modal fade" role="dialog">
        <div className="modal-dialog" role="document">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="text-center" id="myModalLabel">
                {t('USERS.NEW_USER_MODAL.TITLE')}
              </h5>
              <button
                type="button"
                className="close"
                data-dismiss="modal"
                onClick={() => this.clearModalForm()}
              >
                <span>×</span>
              </button>
            </div>
            <div className="modal-body">
              <form className="new-user-form">

                <div className="form-group">
                  <label htmlFor="email" className="col-form-label">
                    {t('USERS.NEW_USER_MODAL.USERNAME')}
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    onChange={(evt) => {
                      this.state.newUser.username = evt.target.value;
                      this.validateUserState();
                    }}
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="email" className="col-form-label">
                    {t('USERS.NEW_USER_MODAL.PASSWORD')}
                  </label>
                  <input
                    type="password"
                    className="form-control"
                    onChange={(evt) => {
                      this.state.newUser.password = evt.target.value;
                      this.validateUserState();
                    }}
                  />
                </div>

                <div className="form-group-row">
                  <label htmlFor="recipient-name" className="col-form-label">
                    {t('USERS.NEW_USER_MODAL.FIRST_NAME')}
                  </label>
                  <input
                    className="form-control"
                    onChange={(evt) => {
                      this.state.newUser.firstName = evt.target.value;
                      this.validateUserState();
                    }}
                  />
                </div>

                <div className="form-group-row">
                  <label htmlFor="recipient-name" className="col-form-label">
                    {t('USERS.NEW_USER_MODAL.LAST_NAME')}
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    onChange={(evt) => {
                      this.state.newUser.lastName = evt.target.value;
                      this.validateUserState();
                    }}
                  />
                </div>

                <div className="form-group">
                  {
                    this.state.emptyFields &&
                    <p className="text-danger mt-2" role="alert">
                      {t('USERS.NEW_USER_MODAL.ALL_FIELDS_REQUIRED')}
                    </p>
                  }
                </div>
              </form>
            </div>
            <div className="modal-footer">
              <div className="form-group col">
                <button
                  type="button"
                  className="btn"
                  disabled={this.state.emptyFields}
                  onClick={() => this.handleUserCreation()}
                  data-dismiss="modal"
                >
                  {t('USERS.NEW_USER_MODAL.ADD_BUTTON')}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export default translate()(NewUserModal);