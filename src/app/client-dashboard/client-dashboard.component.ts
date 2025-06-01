import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { AccountsService } from '../services/accounts.service';
import { AuthService } from '../services/auth.service';
import { BankAccount } from '../admin/models/bank-account.model';
import { AccountOperation } from '../admin/models/account-operation.model';
import { AccountHistory } from '../admin/models/account-history.model';

@Component({
  selector: 'app-client-dashboard',
  templateUrl: './client-dashboard.component.html',
  styleUrls: ['./client-dashboard.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatTableModule
  ]
})
export class ClientDashboardComponent implements OnInit {
  clientId: string | null = null;
  accounts: BankAccount[] = [];
  recentTransactions: AccountOperation[] = [];
  totalBalance: number = 0;
  loadingAccounts: boolean = true;
  loadingTransactions: boolean = true;
  errorAccounts: string | null = null;
  errorTransactions: string | null = null;
  displayedColumns: string[] = ['date', 'type', 'amount', 'description'];
  selectedAccountId: string | null = null;

  constructor(
    private accountsService: AccountsService,
    private authService: AuthService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      this.clientId = this.activatedRoute.snapshot.paramMap.get('clientId');
      console.log('Dashboard clientId:', this.clientId, 'isLoggedIn:', this.authService.isLoggedIn());
      if (!this.clientId || !this.authService.isLoggedIn()) {
        console.warn('Redirecting to login: clientId or auth check failed');
        this.router.navigate(['/login']);
        return;
      }
      this.loadAccounts();
    } else {
      this.loadingAccounts = false;
      this.loadingTransactions = false;
    }
  }

  loadAccounts() {
    if (!this.clientId) {
      this.errorAccounts = 'Client ID is missing';
      this.loadingAccounts = false;
      return;
    }
    this.accountsService.getAccountsByClientId(this.clientId).subscribe({
      next: (data) => {
        console.log('Accounts loaded:', data);
        this.accounts = data;
        this.totalBalance = this.accounts.reduce((total, account) => total + account.balance, 0);
        this.loadingAccounts = false;
        this.errorAccounts = null;
        // Select first account for transactions
        if (data.length > 0) {
          this.selectedAccountId = data[0].id;
          this.loadRecentTransactions();
        }
      },
      error: (err) => {
        this.errorAccounts = err.message || 'Failed to load accounts';
        this.loadingAccounts = false;
        console.error('Accounts load error:', err);
      }
    });
  }

  loadRecentTransactions() {
    if (!this.clientId || !this.selectedAccountId) {
      this.errorTransactions = 'Client ID or Account ID is missing';
      this.loadingTransactions = false;
      return;
    }
    this.accountsService.getRecentTransactions(this.clientId, this.selectedAccountId, 0, 10).subscribe({      next: (data) => {
        console.log('Recent transactions loaded:', data);
        this.recentTransactions = data.accountOperationDTOS;
        this.loadingTransactions = false;
        this.errorTransactions = null;
      },
      error: (err) => {
        this.errorTransactions = err.message || 'Failed to load transactions';
        this.loadingTransactions = false;
        console.error('Transactions load error:', err);
      }
    });
  }

  logout() {
    console.log('Logging out');
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
