/*
 * This app uses a backend endpoint '/auth/login' that returns a JWT token after 
 * successful login.
 * 
 * This file defines the service AuthService, a Singleton that encapsulates the 
 * logic for logging in, i.e., calling the backend '/auth/login' endpoint.
 * 
 * It also saves the JWT token to localStorage to allow the service AuthGuard 
 * (core/guards/auth.guards.ts) to use the method isLoggedIn() to protect routes.
 */

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

/*
* NOTE: Observable is not an Angular component, but a type from RxJS (Reactive 
*       Extensions for JavaScript), which Angular uses heavily for handling 
*       asynchronous operations like HTTP requests.
* 
* NOTE: Type can be seen as a "spy" on the Observable chain. It is an RxJS 
*       operator used for side effects; that is, executing code when something 
*       happens in the stream without modifying the data.
*/
import { Observable, tap } from 'rxjs';

import { jwtDecode } from 'jwt-decode';

/*
* A DTO for the data that the backend sends in response to a successful login request.
*/
interface LoginResponse {
  token: string;
}

/*
* A DTO for JWT token that the backend sends in response to a successful login request.
*/
interface JwtPayload {
  exp: number; // UNIX timestamp (seconds)
}

/* 
 * This class defines a custom authentication service.
 * 
 * NOTE: In Angular, a service is a core building block that is used to 
 *       define reusable logic not related to the UI and also for state management. 
 *       It is often a Singleton.
 * 
 * NOTE: Recalling that singleton is a design pattern used in software engineering 
 *       to ensure that only one instance of a class exists throughout the entire 
 *       lifecycle of an application, and that this instance is globally accessible.
 * 
 * NOTE: In Angular, @Injectable() is a class decorator that marks a class 
 *       as available to be injected as a dependency into other classes via Angular’s 
 *       Dependency Injection (DI) system. This class AuthService is 
 *       injected into the component LoginComponent (features/login/login.component.ts).
 */
@Injectable({
  providedIn: 'root' // Tells Angular to create a Singleton and inject it wherever needed (App-Wide Singleton).
})

export class AuthService {

  private loginUrl = 'http://localhost:8080/auth/login'; // TODO: adjust with full backend URL or proxy

  constructor(private http: HttpClient) {}

  /*
  * The functional goal here is to send login credentials to the backend (via 
  * HttpClient.post), then receive a JWT token if credentials are valid to 
  * finally store the token locally, so the user stays authenticated on future requests.
  */
  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.loginUrl, { username, password }).pipe(
      tap(response => { // NOTE: tap is used here to run side effects without changing the response
        localStorage.setItem('jwtToken', response.token); // Save the JWT token (as a side effect) on successful login
      })
    );
  }

  logout() {
    localStorage.removeItem('jwtToken');
  }

  getToken(): string | null {
    return localStorage.getItem('jwtToken');
  }

  isLoggedIn(): boolean {

    const token = this.getToken();

    if (!token) return false;

    return !this.isTokenExpired();

  }

  isTokenExpired(): boolean {

    const token = this.getToken();

    if (!token) return true;

    try {

      // Decoding the JWT token expiration field (exp) that came encoded in the response payload to check
      const decoded = jwtDecode<JwtPayload>(token);
      const now = Math.floor(Date.now() / 1000);
      return decoded.exp < now;

    } catch {
      return true;
    }
    
  }

  setAutoLogout() {

    const token = this.getToken();
    
    if (!token) return;

      // Decoding the JWT token expiration field (exp) that came encoded in the response payload
    const decoded = jwtDecode<JwtPayload>(token);

    // Calculating how long the token is valid
    const expiresIn = decoded.exp * 1000 - Date.now();

    if (expiresIn > 0) {
      /*
       * NOTE: This is how automatic logout will work:
       * 
       * - setTimeout(...): a built-in JavaScript function that delays the execution of code.
       * 
       * - () => this.logout(): an arrow function that calls your service’s logout() method.
       * 
       * - expiresIn: a number (in milliseconds) that determines how long to wait before executing this.logout().
       */
      setTimeout(() => this.logout(), expiresIn);
    } else {
      this.logout();
    }

  }

}
