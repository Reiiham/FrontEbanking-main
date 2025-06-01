import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Observable, combineLatest, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AccountsService } from '../../services/accounts.service';
import { Beneficiary } from '../../admin/models/beneficiary.model';
import { BankAccount } from '../../admin/models/bank-account.model';
import { MatTableModule } from '@angular/material/table';
import { AsyncPipe, CurrencyPipe, NgForOf, NgIf } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-beneficiary',
  templateUrl: './beneficiary.component.html',
  styleUrls: ['./beneficiary.component.scss'],
  standalone: true,
  imports: [
    RouterLink,
    MatTableModule,
    AsyncPipe,
    MatCardModule,
    CurrencyPipe,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    FormsModule,
    NgForOf,
    NgIf,
  ]
})
export class BeneficiaryComponent implements OnInit {
  clientId: string | null = null;

  accounts$!: Observable<BankAccount[]>;           // comptes internes
  externalAccounts: BankAccount[] = [];             // comptes externes pour select
  externalBeneficiaries$!: Observable<Beneficiary[]>; // bénéficiaires externes

  newBeneficiary: Beneficiary = { rib: '', name: '', accountId: '' };
  errorMessage: string | null = null;
  successMessage: string | null = null;

  displayedColumns: string[] = ['name', 'rib', 'accountId'];

  constructor(
    private accountsService: AccountsService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.clientId = this.route.snapshot.paramMap.get('clientId');
    if (!this.clientId) {
      this.errorMessage = 'Please log in to continue';
      return;
    }
    this.loadAccounts();
    this.loadExternalAccounts();
    this.loadExternalBeneficiaries();
  }

  loadAccounts(): void {
    this.accounts$ = this.accountsService.getAccountsByClientId(this.clientId!).pipe(
      catchError(err => {
        this.errorMessage = err.message || 'Failed to load accounts';
        return throwError(() => err);
      })
    );
  }

  loadExternalAccounts(): void {
    combineLatest([
      this.accountsService.bankAccountsList(),
      this.accountsService.getAccountsByClientId(this.clientId!)
    ]).pipe(
      map(([allAccounts, clientAccounts]: [BankAccount[], BankAccount[]]) => {
        const clientAccountIds = clientAccounts.map((acc: BankAccount) => acc.id);
        return allAccounts.filter((acc: BankAccount) => !clientAccountIds.includes(acc.id));
      }),
      catchError(err => {
        this.errorMessage = err.message || 'Failed to load external accounts';
        return throwError(() => err);
      })
    ).subscribe(accounts => this.externalAccounts = accounts);
  }

  loadExternalBeneficiaries(): void {
    this.externalBeneficiaries$ = combineLatest([
      this.accountsService.getBeneficiaries(this.clientId!),
      this.accountsService.getAccountsByClientId(this.clientId!)
    ]).pipe(
      map(([beneficiaries, clientAccounts]: [Beneficiary[], BankAccount[]]) => {
        const clientAccountIds = clientAccounts.map((acc: BankAccount) => acc.id);
        return beneficiaries.filter(b => !clientAccountIds.includes(b.accountId));
      }),
      catchError(err => {
        this.errorMessage = err.message || 'Failed to load beneficiaries';
        return throwError(() => err);
      })
    );
  }

  addBeneficiary(): void {
    if (!this.newBeneficiary.name || !this.newBeneficiary.rib || !this.newBeneficiary.accountId) {
      this.errorMessage = 'Name, RIB, and Account are required';
      return;
    }
    this.accountsService.addBeneficiary(this.clientId!, this.newBeneficiary).subscribe({
      next: () => {
        this.successMessage = 'Beneficiary added successfully';
        this.newBeneficiary = { rib: '', name: '', accountId: '' };
        this.loadExternalBeneficiaries();
        this.errorMessage = null;
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to add beneficiary';
        this.successMessage = null;
      }
    });
  }
}
