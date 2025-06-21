/*
Angular front-end for the MLOps system

- Sidebar with three routes:

  1. Nominal Composition Manager (with CRUD)
  2. Pre-Deployment Exploration (empty)
  3. Pre-Deployment Exploitation (empty)

  - Nominal Composition Manager performs CRUD via HTTP to FastAPI backend
*/

import { NgModule } from '@angular/core';
import { Routes } from '@angular/router';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { NominalCompositionManagerComponent } from './features/nominal-composition-manager/nominal-composition-manager.component';
import { PreDeploymentExplorationComponent } from './features/pre-deployment-exploration/pre-deployment-exploration.component';
import { PreDeploymentExploitationComponent } from './features/pre-deployment-exploitation/pre-deployment-exploitation.component';
import { LoginComponent } from './features/login/login.component';

const routes: Routes = [
  { path: 'nominal-manager', component: NominalCompositionManagerComponent },
  { path: 'exploration', component: PreDeploymentExplorationComponent },
  { path: 'exploitation', component: PreDeploymentExploitationComponent },
  { path: '', redirectTo: '/nominal-manager', pathMatch: 'full' }
];

@NgModule({
  declarations: [
    AppComponent,
    SidebarComponent,
    NominalCompositionManagerComponent,
    PreDeploymentExplorationComponent,
    PreDeploymentExploitationComponent,
    LoginComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})

export class AppModule {}
