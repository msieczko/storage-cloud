import * as React from 'react';
import SignIn from '../auth/sign-in';
import { AppContext } from './app-hoc';

export interface WithAuthProps {
  refreshAuth: Function;
}

type HOCWrapped<P, PHoc> = React.ComponentClass<P & PHoc> | React.SFC<P & PHoc>;

export function withAuthHoc<P, S>(Component: HOCWrapped<P, WithAuthProps>): React.ComponentClass<P> {

  class C extends React.Component<P & WithAuthProps, S> {
    private first: boolean = true;
    private loading: boolean = true;

    constructor(props: P & WithAuthProps) {
      super(props);
      this.update = this.update.bind(this);
    }

    update() {
      this.forceUpdate();
    }

    public render(): JSX.Element {
      const {refreshAuth, ...rest} = this.props as WithAuthProps;

      return (
        <AppContext.Consumer>
          {
            ({httpsrv}) => {

              if (this.first) {
                this.first = false;
                httpsrv.trySigningInWithExisitingCookie().then(is => {
                  httpsrv.isSignedIn = is;
                  this.loading = false;
                  this.forceUpdate();
                }).catch(() => {
                  httpsrv.isSignedIn = false;
                  this.loading = false;
                  this.forceUpdate();
                });
              }
              if (this.loading) {
                return (<h2>loading, please wait</h2>);
              }

              if (httpsrv.isSignedIn) {
                return (
                  <Component
                    refreshAuth={this.update}
                    {...rest}
                  />);
              } else {
                return <SignIn upd={this.update}/>;
              }
            }}

        </AppContext.Consumer>
      );
    }
  }

  return C;
}

export default withAuthHoc;