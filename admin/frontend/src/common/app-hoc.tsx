import * as React from 'react';
import { AppContextData } from '../index';
import { HttpService } from './http-service';
import { UserDto } from '../users/dto/user-dto';

export interface WithAppProps {
  httpService: HttpService;
  adminUser?: UserDto; // TODO here or in context?
}

export const AppContext: React.Context<AppContextData> = React.createContext({httpsrv: new HttpService()});

type HOCWrapped<P, PHoc> = React.ComponentClass<P & PHoc> | React.SFC<P & PHoc>;

export function withAppHoc<P, S>(Component: HOCWrapped<P, WithAppProps>): React.ComponentClass<P> {

  class C extends React.Component<P & WithAppProps, S> {
    constructor(props: P & WithAppProps) {
      super(props);
    }

    public render(): JSX.Element {
      const {httpService, ...rest} = this.props as WithAppProps;
      return (
        <AppContext.Consumer>
          {
            ({httpsrv}) => {
              return (
                <Component
                  httpService={httpsrv}
                  {...rest}
                />);
            }}
        </AppContext.Consumer>
      );
    }
  }

  return C;
}

export default withAppHoc;