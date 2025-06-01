import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Observable, throwError, combineLatest, of } from 'rxjs';
import { catchError, tap, switchMap } from 'rxjs/operators';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AccountsService } from '../../services/accounts.service';
import { BankAccount } from '../../admin/models/bank-account.model';
import { TransferRequest } from '../../admin/models/transfer-request.model';
import { Beneficiary } from '../../admin/models/beneficiary.model';
import { AccountOperation } from '../../admin/models/account-operation.model';

@Component({
  selector: 'app-transfer',
  templateUrl: './transfer.component.html',
  styleUrls: ['./transfer.component.scss'],
  imports: [
    CommonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatTableModule,
    FormsModule,
    RouterModule
  ],
  standalone: true
})
export class TransferComponent implements OnInit {
  clientId!: string;
  accounts$!: Observable<BankAccount[]>;
  beneficiaries$!: Observable<Beneficiary[]>;
  operations$!: Observable<AccountOperation[] | null>;
  transfer: TransferRequest = { accountSource: '', accountDestination: '', amount: 0 };
  errorMessage: string | null = null;
  successMessage: string | null = null;
  remainingBalance: number | null = null;
  displayedColumns: string[] = ['date', 'type', 'amount', 'description'];
  loadingTransactions: boolean = false;

  constructor(
    private accountsService: AccountsService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.clientId = this.route.snapshot.params['clientId'];
    this.loadAccounts();
    this.loadBeneficiaries();
    this.loadOperations(); // Initial load of operations
  }

  loadAccounts(): void {
    this.accounts$ = this.accountsService.getClientAccounts(this.clientId).pipe(
      catchError(err => {
        this.errorMessage = err.message || 'Failed to load accounts';
        return throwError(() => err);
      })
    );
  }

  loadBeneficiaries(): void {
    this.beneficiaries$ = this.accountsService.getBeneficiaries(this.clientId).pipe(
      catchError(err => {
        this.errorMessage = err.message || 'Failed to load beneficiaries';
        return throwError(() => err);
      })
    );
  }

  loadOperations(): void {
    if (this.transfer.accountSource) {
      this.loadingTransactions = true;
      this.operations$ = this.accountsService.getAccountOperations(this.clientId, this.transfer.accountSource).pipe(
        tap(() => this.loadingTransactions = false),
        catchError(err => {
          this.loadingTransactions = false;
          this.errorMessage = err.message || 'Failed to load operations';
          return throwError(() => err);
        })
      );
    } else {
      this.operations$ = of(null);
    }
  }

  onSourceAccountChange(): void {
    this.loadOperations();
  }

  transferFunds(): void {
    if (!this.transfer.accountSource || !this.transfer.accountDestination || this.transfer.amount <= 0) {
      this.errorMessage = 'Source account, destination RIB, and a positive amount are required';
      this.successMessage = null;
      return;
    }

    this.errorMessage = null;
    this.successMessage = null;
    this.remainingBalance = null;

    this.accountsService.transferFunds(this.clientId, this.transfer).pipe(
      switchMap(() => this.accountsService.getAccountBalance(this.clientId, this.transfer.accountSource))
    ).subscribe({
      next: (balance) => {
        this.remainingBalance = balance;
        this.successMessage = 'Transaction réalisée avec succès';
        this.errorMessage = null;
        this.transfer = { accountSource: '', accountDestination: '', amount: 0 }; // Reset form
        this.loadOperations(); // Refresh transactions
      },
      error: (err) => {
        this.errorMessage = err.message || 'Transfer failed';
        this.successMessage = null;
      }
    });
  }
}
