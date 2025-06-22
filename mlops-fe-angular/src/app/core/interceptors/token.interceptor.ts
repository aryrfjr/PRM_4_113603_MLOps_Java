/*
 * This app uses a backend endpoint '/auth/login' that returns a JWT token after 
 * successful login.
 * 
 * So, after sending the login request to the backend, this Angular app must 
 * store the JWT token securely (in localStorage) to protect the rest of the 
 * app behind a guard that checks if the token is present and valid.
 */
import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent
} from '@angular/common/http';
import { Observable } from 'rxjs';

import { AuthService } from '../services/auth.service';

/* 
 * This class defines a special Angular HTTP interceptor that automatically 
 * attaches the JWT token to outgoing HTTP requests.
 * 
 * NOTE: In Angular, @Injectable() is a class decorator that marks a class 
 *       as available to be injected as a dependency into other classes via Angular’s 
 *       Dependency Injection (DI) system. This class TokenInterceptor is 
 *       injected into the feature module AppModule (app.module.ts).
 */
@Injectable()
export class TokenInterceptor implements HttpInterceptor {

  /*
   * Here, TokenInterceptor depends on AuthService. To allow Angular to inject an 
   * instance of AuthService into the constructor, Angular needs to know that 
   * TokenInterceptor participates in DI. Without @Injectable(), Angular will not 
   * generate the metadata it needs to resolve dependencies like AuthService.
   */
  constructor(private authService: AuthService) {}

  /*
   * It intercepts before the request is sent to the server, so that it won't 
   * be necessary to manually attach the token in every service.
   */
  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    
    const token = this.authService.getToken();

    // Attaching the JWT token to outgoing HTTP requests
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request);
    
  }

}
