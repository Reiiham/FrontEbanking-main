import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { EmployeeDashboardComponent } from './employee-dashboard/employee-dashboard.component';
import { ClientDashboardComponent } from './client-dashboard/client-dashboard.component';
import { CustomerAccountsComponent } from './components/customer-accounts/customer-accounts.component';
import { AccountDetailsComponent } from './components/account-details/account-details.component';
import { TransactionsComponent } from './components/transactions/transactions.component';
import { TransferComponent } from './components/transfer/transfer.component';
import { BeneficiaryComponent } from './components/beneficiary/beneficiary.component';
import { AuthGuard } from './auth.guard';
import { ResolveFn } from '@angular/router';
import { AccountsService } from './services/accounts.service';
import { BankAccount } from './admin/models/bank-account.model';
import { inject } from '@angular/core';

const accountResolver: ResolveFn<BankAccount> = (route) => {
  const accountsService = inject(AccountsService);
  const clientId = route.paramMap.get('clientId');
  const accountId = route.paramMap.get('accountId');
  if (!clientId || !accountId) {
    throw new Error('Client ID or Account ID missing');
  }
  return accountsService.getAccount(clientId, accountId);
};

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: 'admin',
    loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule),
    canActivate: [AuthGuard]
  },
  { path: 'employee/dashboard', component: EmployeeDashboardComponent, canActivate: [AuthGuard] },
  { path: 'client/:clientId/dashboard', component: ClientDashboardComponent, canActivate: [AuthGuard] },
  {
    path: 'client/:clientId/accounts',
    component: CustomerAccountsComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'client/:clientId/accounts/:accountId',
    component: AccountDetailsComponent,
    canActivate: [AuthGuard],
    resolve: { account: accountResolver }
  },
  {
    path: 'client/:clientId/accounts/:accountId/transactions',
    component: TransactionsComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'client/:clientId/transfer',
    component: TransferComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'client/:clientId/beneficiaries',
    component: BeneficiaryComponent,
    canActivate: [AuthGuard]
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];
