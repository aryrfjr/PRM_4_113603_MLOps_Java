/*
 * This app has a Sidebar with options that are associated to the 
 * routes defined in this feature module. Flow is redirect to login 
 * if unauthenticated.
 */

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './features/login/login.component';
import { NominalCompositionManagerComponent } from './features/nominal-composition-manager/nominal-composition-manager.component';
import { PreDeploymentExplorationComponent } from './features/pre-deployment-exploration/pre-deployment-exploration.component';
import { PreDeploymentExploitationComponent } from './features/pre-deployment-exploitation/pre-deployment-exploitation.component';
import { AuthGuard } from './core/guards/auth.guard';

// Defining the route table
const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', redirectTo: '/nominal-composition-manager', pathMatch: 'full' },
  {
    path: 'nominal-composition-manager',
    component: NominalCompositionManagerComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'pre-deployment-exploration',
    component: PreDeploymentExplorationComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'pre-deployment-exploitation',
    component: PreDeploymentExploitationComponent,
    canActivate: [AuthGuard],
  },
  { path: '**', redirectTo: '/login' } // Fallback route
];

@NgModule({
  imports: [RouterModule.forRoot(routes)], // Importing the route table into the router
  exports: [RouterModule] // Exporting the router that will be used as the tag <router-outlet> in app.component.html
})

export class AppRoutingModule {} // returning the feature module declared in this file
