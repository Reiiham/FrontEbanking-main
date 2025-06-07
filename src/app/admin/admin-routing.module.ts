import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminLayoutComponent } from './admin-layout/admin-layout.component';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { CurrencyListComponent } from './currency/currency-list/currency-list.component';
import { SettingsListComponent } from './settings/settings-list/settings-list.component';
import { CurrencyFormComponent } from './currency/currency-form/currency-form.component';
import { SettingsEditFormComponent } from './settings/settings-edit-form/settings-edit-form.component';
import { AddEmployeeComponent } from './add-employee/add-employee.component';

const routes: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'currencies', component: CurrencyListComponent },
      { path: 'currencies/edit/:id', component: CurrencyFormComponent },
      { path: 'settings', component: SettingsListComponent },
      { path: 'settings/edit/:id', component: SettingsEditFormComponent },
      { path: 'create-employee', component: AddEmployeeComponent }
    ]
  }
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule {}
