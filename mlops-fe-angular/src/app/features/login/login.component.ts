/*
 * This app uses a backend endpoint '/auth/login' that returns a JWT token after 
 * successful login.
 * 
 * This file defines LoginComponent, an UI + logic + view for a login 
 * page that accepts username and password
 */
import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login', // NOTE: not referenced anywhere
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})

export class LoginComponent {

  username = '';
  password = '';
  errorMessage = '';

  /*
   * When the LoginComponent is referenced in AppRoutingModule (app-routing.module.ts) 
   * and in AppModule (app.module.ts) Angular:
   * 
   * (I) Instantiates the component.
   * 
   * (II) Automatically injects the dependencies declared in its constructor; 
   *      which are the custom service AuthService (core/services/auth.service.ts) 
   *      and the Angular built-in service Router.
   */
  constructor(private authService: AuthService, private router: Router) {}

  // This method is used in the tag <form> or the UI defined in login.component.html,
  // with username and password the ids of corresponding tags <input>
  onSubmit() {
    this.authService.login(this.username, this.password).subscribe({
      next: () => { // As per definition in AppRoutingModule (app-routing.module.ts) goes to /nominal-composition-manager
        this.router.navigate(['/']);
      },
      error: () => {
        this.errorMessage = 'Invalid username or password';
      }
    });
  }

}
