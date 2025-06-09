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
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

const routes: Routes = [
 
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
      LayoutComponent,
      DashboardComponent,
      ClientListComponent,
      ClientDetailsComponent,
      ClientCreateComponent,
      ClientUpdateComponent,
      ClientDeleteComponent,
      ClientStatusToggleComponent
]

})
export class BanqueModule {}
