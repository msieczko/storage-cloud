export class HttpService {

  public isSignedIn: boolean = false;

  trySigningInWithExisitingCookie(): Promise<boolean> {
    return new Promise(resolve => {
      if (!this.isSignedIn) {
        resolve(this.getUserInfo());
      }
      resolve(this.isSignedIn);
    });
  }

  signIn(username: string, password: string): Promise<string> {
    return new Promise((resolve, reject) => {
      console.log('trying to sign in with username: ' + username);
      const userAuthDto = {'username': username, 'password': password};
      this.post('/api/login', userAuthDto)
        .then((response) => {
          if (response instanceof Response && response.ok) {
            response.json().then((data: { success: boolean, errorMessage: string }) => {
              if (data.success) {
                this.isSignedIn = true;
                console.log('signed in with ' + username);
                resolve('OK');
              } else {
                console.log('sign in error');
                reject(data.errorMessage);
              }
            });
          } else {
            reject('Failed to connect to server');
          }
        }).catch(reason => reject('Failed to connect to server: ' + reason));
    });
  }

  post(path: string, data: object): Promise<{} | Response> {
    // TODO error / unathorized checking
    return this.fetchWithTimeout(path, {
      method: 'POST',
      'headers': {'Content-type': 'application/json;charset=UTF-8'},
      credentials: 'same-origin',
      body: JSON.stringify(data)
    });
  }

  get(path: string): Promise<Response> {
    return new Promise((resolve, reject) => {
      window.fetch(path, {
        method: 'GET',
        'headers': {'Content-type': 'application/json;charset=UTF-8'},
        credentials: 'same-origin',
      }).then(value => {
        if (!value.ok) {
          this.isSignedIn = false;
          value.text().then(text => reject(text)).catch(reason => reject(reason));
        }
        resolve(value);
      });
    });
  }

  getUserInfo(): Promise<boolean> {
    // TODO cache user info
    return new Promise((resolve, reject) => {
      this.fetchWithTimeout('/api/login', {credentials: 'same-origin'})
        .then(response => {
          if (response instanceof Response && response.ok) {
            response.json().then(data => {
              console.log(`b: ${data}`);
              if (data.authenticated) {
                resolve(data.authenticated);
              } else {
                reject('user is not signed in');
              }
            });
          } else {
            reject('could not reach server');
          }
        })
        .catch(reason => {
          reject(reason);
        });
    });

  }

  fetchWithTimeout(url: string, options?: RequestInit, timeout: number = 5000): Promise<{} | Response> {
    return Promise.race([
      window.fetch(url, options),
      new Promise((_, reject) =>
        setTimeout(() => reject('timeout'), timeout)
      )
    ]);
  }
}