import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LayoutComponent } from './components/layout/layout.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ClientListComponent } from './components/client-list/client-list.component';
import { ClientDetailsComponent } from './components/client-details/client-details.component';
import { ClientCreateComponent } from './components/client-create/client-create.component';
import { ClientUpdateComponent } from './components/client-update/client-update.component';
import { ClientDeleteComponent } from './components/client-delete/client-delete.component';
import { ClientStatusToggleComponent } from './components/client-status-toggle/client-status-toggle.component';
import { TransactionSelectorComponent } from './components/transaction-selector/transaction-selector.component';
import {VirementComponent} from './components/virement/virement.component';
import { OperationComponent } from './components/operation/operation.component';
import {AddAccountComponent} from './components/add-account/add-account.component';

const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: '', component: DashboardComponent },
      { path: 'clients', component: ClientListComponent },
      { path: 'clients/create', component: ClientCreateComponent },
      { path: 'clients/:id', component: ClientDetailsComponent },
      { path: 'clients/:id/edit', component: ClientUpdateComponent },
      { path: 'clients/:id/delete', component: ClientDeleteComponent },
      { path: 'clients/:id/status', component: ClientStatusToggleComponent },
      { path: 'clients/:id/add-account', component: AddAccountComponent},

      {
        path: 'transactions',
        component: TransactionSelectorComponent,
        children: [
          { path: 'virement', component: VirementComponent },
          { path: 'operation', component: OperationComponent }
        ]
      }
    ]
  }
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BanqueRoutingModule {}
