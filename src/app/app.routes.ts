import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';

import { ClientDashboardComponent } from './client-dashboard/client-dashboard.component';
//import { CustomerAccountsComponent } from './components/customer-accounts/customer-accounts.component';
//import { AccountDetailsComponent } from './components/account-details/account-details.component';
//import { TransactionsComponent } from './components/transactions/transactions.component';
//import { TransferComponent } from './components/transfer/transfer.component';
//import { BeneficiaryComponent } from './components/beneficiary/beneficiary.component';

import { SetPasswordComponent } from './components/set-password/set-password.component';
import { AuthGuard } from './auth.guard';
import { ResolveFn } from '@angular/router';
import { inject } from '@angular/core';

//import { AccountsService } from './services/accounts.service';
import { BankAccount } from './admin/models/bank-account.model';

// Resolver pour charger les détails d’un compte
/*
const accountResolver: ResolveFn<BankAccount> = (route) => {
  const accountsService = inject(AccountsService);
  const clientId = route.paramMap.get('clientId');
  const accountId = route.paramMap.get('accountId');
  if (!clientId || !accountId) {
    throw new Error('Client ID or Account ID missing');
  }
  return accountsService.getAccount(clientId, accountId);
};

 */

export const routes: Routes = [


  // 🔐 Auth routes
  { path: 'login', component: LoginComponent },
  { path: 'set-password', component: SetPasswordComponent },

  // 🛡️ Admin (lazy loading)

  {
    path: 'admin',
    loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule),
    canActivate: [AuthGuard]
  },

  // 🧑‍💼 Employé (Banque)
  {
    path: 'employee',
    loadChildren: () => import('./banque/banque.module').then(m => m.BanqueModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'clients',
    loadComponent: () => import('./banque/components/client-list/client-list.component').then(m => m.ClientListComponent)
  },

  // 👤 Client space
  {
    path: 'client/:clientId/dashboard',
    component: ClientDashboardComponent,
    canActivate: [AuthGuard]
  },
  // {
  //   path: 'client/:clientId/qr-payment',
  //   loadComponent: () => import('./qr-payment/qr-payment.component').then(m => m.QrPaymentComponent),
  //   canActivate: [AuthGuard]
  // },

/*
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
*/

  // 🏠 Default and wildcard routes

  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];

