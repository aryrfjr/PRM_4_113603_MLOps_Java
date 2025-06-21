import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NominalCompositionManagerComponent } from './features/nominal-composition-manager/nominal-composition-manager.component';
import { PreDeploymentExplorationComponent } from './features/pre-deployment-exploration/pre-deployment-exploration.component';
import { PreDeploymentExploitationComponent } from './features/pre-deployment-exploitation/pre-deployment-exploitation.component';

const routes: Routes = [
  { path: 'nominal-compositions', component: NominalCompositionManagerComponent },
  { path: 'pre-deployment-exploration', component: PreDeploymentExplorationComponent },
  { path: 'pre-deployment-exploitation', component: PreDeploymentExploitationComponent },
  { path: '', redirectTo: 'nominal-compositions', pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})

export class AppRoutingModule {}
