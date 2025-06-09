import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { BanqueRoutingModule } from './banque-routing.module';

import { LayoutComponent } from './components/layout/layout.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ClientListComponent } from './components/client-list/client-list.component';
import { ClientDetailsComponent } from './components/client-details/client-details.component';
import { ClientCreateComponent } from './components/client-create/client-create.component';
import { ClientUpdateComponent } from './components/client-update/client-update.component';
import { ClientDeleteComponent } from './components/client-delete/client-delete.component';
import { ClientStatusToggleComponent } from './components/client-status-toggle/client-status-toggle.component';

// Toastr support (si tu l'utilises ici)
import { ToastrModule } from 'ngx-toastr';
import {AuthGuard} from '../auth.guard';
import {VirementComponent} from './components/virement/virement.component';
import { AddAccountComponent } from './components/add-account/add-account.component';


const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: '', component: DashboardComponent },
      { path: 'clients', component: ClientListComponent },
      { path: 'clients/create', component: ClientCreateComponent },
      { path: 'clients/:id', component: ClientDetailsComponent},
      { path: 'clients/:id/edit', component: ClientUpdateComponent },
      { path: 'clients/:id/add-account', component: AddAccountComponent },
      { path: 'clients/:id/delete', component: ClientDeleteComponent },
      { path: 'clients/:id/status', component: ClientStatusToggleComponent }
    ]
  }
];

@NgModule({
  declarations: [

  ],

  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    BanqueRoutingModule,
    ToastrModule,
    VirementComponent,
    // Optionnel si déjà importé globalement dans AppModule
  ]

})
export class BanqueModule {}
