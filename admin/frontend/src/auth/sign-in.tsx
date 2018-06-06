import * as React from 'react';
import { default as withAppHoc, WithAppProps } from '../common/app-hoc';
import { i18n } from 'i18next';
import { translate } from 'react-i18next';

interface SignInState {
  displayWarning: Boolean;
  warningMessage: string;
}

class SignIn extends React.Component<{ t: Function, i18n: i18n, upd: Function } & WithAppProps, SignInState> {
  private email: string;
  private password: string;

  constructor(props: { t: Function, i18n: i18n, upd: Function } & WithAppProps) {
    super(props);
    this.state = {
      displayWarning: false,
      warningMessage: ''
    };

    this.signIn = this.signIn.bind(this);
  }

  signIn() {
    this.props.httpService.signIn(this.email, this.password)
      .then(() => {
        this.props.upd();
      }).catch((msg: string) => {
      this.setState({displayWarning: true, warningMessage: msg});
    });
  }

  onEnterKey(e: React.KeyboardEvent<HTMLInputElement>, action: Function) {
    if (e.key === 'Enter') {
      action();
    }
  }

  render() {
    const {t} = this.props;
    return (
      <div className="container">
        <div className="row">
          <div className="offset-md-4 col-md-4 mt-5">
            <div className="card">
              <h5 className="card-header">{t('SIGN_IN.WELCOME_MSG')}</h5>
              <div className="card-body">
                {
                  this.state.displayWarning &&
                  <div className="alert alert-danger" role="alert">{this.state.warningMessage}</div>
                }
                <form>
                  <div className="form-group row">
                    <div className="col">
                      <input
                        type="email"
                        className="form-control"
                        id="signInEmailInput"
                        aria-describedby="emailHelp"
                        placeholder={t('SIGN_IN.USERNAME_HINT')}
                        onChange={e => this.email = e.target.value}
                        onKeyPress={e => this.onEnterKey(e, this.signIn)}
                      />
                    </div>
                  </div>
                  <div className="form-group row">
                    <div className="col">
                      <input
                        type="password"
                        className="form-control"
                        id="signInPasswordInput"
                        placeholder={t('SIGN_IN.PASSWORD_HINT')}
                        onChange={e => this.password = e.target.value}
                        onKeyPress={e => this.onEnterKey(e, this.signIn)}
                      />
                    </div>
                  </div>
                </form>
                <button className="btn btn-primary btn-block" onClick={this.signIn}>{t('SIGN_IN.SUBMIT_BUTTON')}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export default translate()(withAppHoc(SignIn));