/*

Angular front-end for the MLOps system

=====================

- Sidebar with three routes:
  1. Nominal Composition Manager (with CRUD)
  2. Pre-Deployment Exploration (empty)
  3. Pre-Deployment Exploitation (empty)

  - Nominal Composition Manager performs CRUD via HTTP to FastAPI backend

*/

// app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';

import { AppComponent } from './app.component';
import { SidebarComponent } from './sidebar/sidebar.component';
import { NominalManagerComponent } from './nominal-manager/nominal-manager.component';
import { ExplorationComponent } from './exploration/exploration.component';
import { ExploitationComponent } from './exploitation/exploitation.component';

const routes: Routes = [
  { path: 'nominal-manager', component: NominalManagerComponent },
  { path: 'exploration', component: ExplorationComponent },
  { path: 'exploitation', component: ExploitationComponent },
  { path: '', redirectTo: '/nominal-manager', pathMatch: 'full' }
];

@NgModule({
  declarations: [
    AppComponent,
    SidebarComponent,
    NominalManagerComponent,
    ExplorationComponent,
    ExploitationComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    RouterModule.forRoot(routes)
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {}

