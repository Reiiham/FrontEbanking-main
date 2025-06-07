import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import { AccountsService } from '../../services/accounts.service';
import { AccountOperation } from '../../admin/models/account-operation.model';

@Component({
  selector: 'app-transactions',
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.scss'],
  standalone: true,
  imports: [CommonModule, MatTableModule, MatCardModule, MatButtonModule, RouterModule]
})
export class TransactionsComponent implements OnInit {
  clientId: string | null = null;
  accountId: string | null = null;
  operations$!: Observable<AccountOperation[]>;
  errorMessage: string | null = null;
  displayedColumns: string[] = ['date', 'type', 'amount', 'description'];

  constructor(
    private accountsService: AccountsService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.clientId = this.route.snapshot.paramMap.get('clientId');
    this.accountId = this.route.snapshot.paramMap.get('accountId');
    console.log('TransactionsComponent clientId:', this.clientId, 'accountId:', this.accountId);
    if (!this.clientId || !this.accountId) {
      this.errorMessage = 'Invalid client or account ID';
      return;
    }
    this.operations$ = this.accountsService.getAccountOperations(this.clientId, this.accountId).pipe(
      catchError(err => {
        this.errorMessage = err.message || 'Failed to load transactions';
        return throwError(() => err);
      })
    );
  }
}
