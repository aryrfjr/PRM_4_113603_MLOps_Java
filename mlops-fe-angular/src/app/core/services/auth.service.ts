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
import { Observable, tap } from 'rxjs';

interface LoginResponse {
  token: string;
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

  private loginUrl = '/auth/login'; // TODO: adjust with full backend URL or proxy

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.loginUrl, { username, password }).pipe(
      tap(response => {
        localStorage.setItem('jwtToken', response.token); // Save token on successful login
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
    return !!this.getToken();
  }

}
