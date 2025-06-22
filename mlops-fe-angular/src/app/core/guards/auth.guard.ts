/*
 * This app uses a backend endpoint '/auth/login' that returns a JWT token after 
 * successful login.
 * 
 * This file defines the service AuthGuard, a Singleton that encapsulates the 
 * logic for making fast, stateless decisions about whether to allow or block 
 * route access.
 * 
 * It also saves the JWT token to localStorage to allow the service AuthGuard 
 * (core/guards/auth.guards.ts) to use the method isLoggedIn() to protect routes.
 */

import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/* 
 * This class defines a custom guart service.
 * 
 * NOTE: In Angular, a service is a core building block that is used to 
 *       define reusable logic not related to the UI and also for state management. 
 *       It is often a Singleton.
 * 
 * NOTE: In Angular, @Injectable() is a class decorator that marks a class 
 *       as available to be injected as a dependency into other classes via Angular’s 
 *       Dependency Injection (DI) system. This class AuthGuard is 
 *       injected into the Angular built-in component RouterModule.
 */
@Injectable({
  providedIn: 'root' // Tells Angular to create a Singleton and inject it wherever needed (App-Wide Singleton).
})

export class AuthGuard implements CanActivate {

    /*
   * When AuthGuard is referenced in AppRoutingModule (app-routing.module.ts) to 
   * define the route table, Angular:
   * 
   * (I) Instantiates the component.
   * 
   * (II) Automatically injects the dependencies declared in its constructor; 
   *      which are the custom service AuthService (core/services/auth.service.ts) 
   *      and the Angular built-in service Router.
   */
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {

    if (this.authService.isLoggedIn()) {
      return true;
    }

    this.router.navigate(['/login']);

    return false;

  }

}
