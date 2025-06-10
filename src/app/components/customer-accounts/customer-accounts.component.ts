import { Component, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { AccountsService } from '../../services/accounts.service';
import { AuthService } from '../../services/auth.service';
import { BankAccount } from '../../admin/models/bank-account.model';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-customer-accounts',
  templateUrl: './customer-accounts.component.html',
  styleUrls: ['./customer-accounts.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    RouterModule
  ]
})
export class CustomerAccountsComponent {
  clientId: string | null = null;
  accounts$!: Observable<BankAccount[]>;
  displayedColumns: string[] = ['id', 'status', 'type', 'dateCreatedAt', 'balance', 'overDraft', 'actions'];
  errorMessage: string = '';

  constructor(
    private accountsService: AccountsService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    if (isPlatformBrowser(this.platformId)) {
      this.clientId = this.route.snapshot.paramMap.get('clientId') || this.authService.getClientId();
      console.log('CustomerAccounts clientId:', this.clientId);
    }
    if (!this.clientId) {
      this.errorMessage = 'Please log in to continue';
      this.accounts$ = of([]);
      return;
    }
    this.accounts$ = this.accountsService.getAccountsByClientId(this.clientId).pipe(
      catchError(err => {
        this.errorMessage = err.message || 'Failed to load accounts';
        return of([]);
      })
    );
  }

  viewDetails(accountId: string): void {
    if (this.clientId) {
      this.router.navigate([`/client/${this.clientId}/accounts/${accountId}`]);
    }
  }

  viewTransactions(accountId: string): void {
    if (this.clientId) {
      this.router.navigate([`/client/${this.clientId}/accounts/${accountId}/transactions`]);
    }
  }
}
