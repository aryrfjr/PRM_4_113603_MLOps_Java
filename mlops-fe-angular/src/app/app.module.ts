/*
 * This is the main module for this Angular front-end developed for the MLOps system.
 *
 * It declares a feature module (AppModule) decorated with @NgModule,
 * which groups related components root components, other feature modules, 
 * and an interceptor.
 */

import { NgModule } from '@angular/core'; // decorator
import { BrowserModule } from '@angular/platform-browser'; // @NgModule
import { FormsModule } from '@angular/forms'; // @NgModule
import { HttpClientModule } from '@angular/common/http'; // @NgModule
import { HTTP_INTERCEPTORS } from '@angular/common/http'; // constant

import { AppComponent } from './app.component'; // @Component
import { AppRoutingModule } from './app-routing.module'; // @NgModule
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { NominalCompositionManagerComponent } from './features/nominal-composition-manager/nominal-composition-manager.component'; // @Component
import { PreDeploymentExplorationComponent } from './features/pre-deployment-exploration/pre-deployment-exploration.component'; // @Component
import { PreDeploymentExploitationComponent } from './features/pre-deployment-exploitation/pre-deployment-exploitation.component'; // @Component
import { LoginComponent } from './features/login/login.component'; // @Component
import { TokenInterceptor } from './core/interceptors/token.interceptor'; // @Injectable

/* 
 * NOTE: In Angular (and TypeScript in general), a decorator is a special 
 *       kind of declaration that adds metadata to a class, method, property, 
 *       or parameter.
 * 
 * NOTE: Here, @NgModule is a decorator used to define an Angular module, 
 *       whereas AppModule is an instance of what @NgModule defines.
 * 
 * NOTE: An NgModule is a class marked by the @NgModule decorator. This decorator 
 *       accepts metadata that tells Angular how to compile component templates and 
 *       configure dependency injection. See more at:
 *       https://angular.dev/guide/ngmodules/overview
 */
@NgModule({
   // The set of Angular core building blocks (components, directives, and pipes) 
   // that belong to this module
  declarations: [
    AppComponent,
    SidebarComponent,
    NominalCompositionManagerComponent,
    PreDeploymentExplorationComponent,
    PreDeploymentExploitationComponent,
    LoginComponent
  ],
   // The set of NgModules whose exported declarables are available to templates 
   // in this module
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    AppRoutingModule
  ],
   // The set of Angular core building blocks (components, directives, and pipes) 
   // declared in this NgModule that can be used in the template of any component 
   // that is part of an NgModule that imports this NgModule
  exports: [],
  // The set of injectable objects that are available in the injector of this module
  //
  // In this case, Angular is configured to run an interceptor on every HTTP request made using HttpClient
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor, // NOTE: Implemented in core/interceptors/token.interceptor.ts
      multi: true
    }
  ],
  bootstrap: [AppComponent] // AppComponent will be bootstrapped when this module is bootstrapped
})

export class AppModule {} // returning the feature module declared in this file
