/*
import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map, timeout } from 'rxjs/operators';
import { environment } from '../environments/environment';
import { BankAccount } from '../admin/models/bank-account.model';
import { AccountOperation } from '../admin/models/account-operation.model';
import { TransferRequest } from '../admin/models/transfer-request.model';
import { DebitCredit } from '../admin/models/debit-credit.model';
import { Beneficiary } from '../admin/models/beneficiary.model';
import { AccountHistory } from '../admin/models/account-history.model';

@Injectable({
  providedIn: 'root'
})
export class AccountsService {
  private apiUrl = `${environment.apiUrl}/clients`;
  private readonly REQUEST_TIMEOUT = 10000;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  private getHeaders(): HttpHeaders {
    const token = isPlatformBrowser(this.platformId) ? localStorage.getItem('token') : null;
    console.log('Token for request:', token);
    if (!token) {
      throw new Error('No token found. Please log in.');
    }
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  private handleError(error: any): Observable<never> {
    const errorMessage = error.status === 0
      ? 'Network error: Please check your internet connection'
      : `Error ${error.status || 'unknown'}: ${error.error?.message || error.message || 'An error occurred'}`;
    console.error('API error:', error);
    return throwError(() => new Error(errorMessage));
  }

  getRecentTransactions(clientId: string, accountId: string, number: number, number1: number): Observable<AccountHistory> {
    if (!clientId || !accountId) {
      return throwError(() => new Error('Client ID and Account ID are required'));
    }
    const url = `${this.apiUrl}/${clientId}/accounts/${accountId}/operations`;
    console.log('Fetching transactions from:', url);
    return this.http.get<AccountOperation[]>(url, { headers: this.getHeaders() }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      map(operations => {
        console.log('Raw transactions response:', operations);
        return {
          accountOperationDTOS: operations || [],
          accountId: accountId,
          balance: 0,
          currentPage: 0,
          pageSize: operations.length,
          totalPages: 1,
          total: operations.length
        };
      }),
      catchError(this.handleError)
    );
  }

  getAccountsByClientId(clientId: string): Observable<BankAccount[]> {
    if (!clientId) {
      return throwError(() => new Error('Client ID is required'));
    }
    return this.http.get<BankAccount[] | null>(`${this.apiUrl}/${clientId}/accounts`, { headers: this.getHeaders() }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      map(accounts => accounts ?? []),
      catchError(this.handleError)
    );
  }

  getAccounts(clientId: string): Observable<BankAccount[]> {
    return this.getAccountsByClientId(clientId);
  }

  getAccount(clientId: string, accountId: string): Observable<BankAccount> {
    if (!clientId || !accountId) {
      return throwError(() => new Error('Client ID and Account ID are required'));
    }
    return this.http.get<BankAccount>(`${this.apiUrl}/${clientId}/accounts/${accountId}`, { headers: this.getHeaders() }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      catchError(this.handleError)
    );
  }

  getAccountOperations(clientId: string, accountId: string): Observable<AccountOperation[]> {
    if (!clientId || !accountId) {
      return throwError(() => new Error('Client ID and Account ID are required'));
    }
    return this.http.get<AccountOperation[]>(`${this.apiUrl}/${clientId}/accounts/${accountId}/operations`, { headers: this.getHeaders() }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      catchError(this.handleError)
    );
  }

  debitAccount(clientId: string, accountId: string, debit: DebitCredit): Observable<DebitCredit> {
    if (!clientId || !accountId || !debit) {
      return throwError(() => new Error('Client ID, Account ID, and debit data are required'));
    }
    return this.http.post<DebitCredit>(`${this.apiUrl}/${clientId}/accounts/${accountId}/debit`, debit, { headers: this.getHeaders() }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      catchError(this.handleError)
    );
  }

  creditAccount(clientId: string, accountId: string, credit: DebitCredit): Observable<DebitCredit> {
    if (!clientId || !accountId || !credit) {
      return throwError(() => new Error('Client ID, Account ID, and credit data are required'));
    }
    return this.http.post<DebitCredit>(`${this.apiUrl}/${clientId}/accounts/${accountId}/credit`, credit, { headers: this.getHeaders() }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      catchError(this.handleError)
    );
  }

  transferFunds(clientId: string, transfer: TransferRequest): Observable<string> {
    if (!clientId || !transfer || !transfer.accountSource || !transfer.accountDestination || transfer.amount <= 0) {
      return throwError(() => new Error('Client ID, transfer data, source account, destination account, and a positive amount are required'));
    }
    return this.http.post<string>(`${this.apiUrl}/${clientId}/accounts/transfer`, transfer, { headers: this.getHeaders() }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      catchError(this.handleError)
    );
  }

  addBeneficiary(clientId: string, beneficiary: Beneficiary): Observable<Beneficiary> {
    if (!clientId || !beneficiary || !beneficiary.rib || !beneficiary.name || !beneficiary.accountId) {
      return throwError(() => new Error('Client ID, beneficiary data, RIB, name, and account ID are required'));
    }
    return this.http.post<Beneficiary>(`${this.apiUrl}/${clientId}/beneficiaries`, beneficiary, { headers: this.getHeaders() }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      catchError(this.handleError)
    );
  }

  getBeneficiaries(clientId: string): Observable<Beneficiary[]> {
    if (!clientId) {
      return throwError(() => new Error('Client ID is required'));
    }
    return this.http.get<Beneficiary[] | null>(`${this.apiUrl}/${clientId}/beneficiaries`, { headers: this.getHeaders() }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      map(beneficiaries => beneficiaries ?? [] as Beneficiary[]),
      catchError(this.handleError)
    );
  }

  getClientAccounts(clientId: string): Observable<BankAccount[]> {
    if (!clientId) {
      return throwError(() => new Error('Client ID is required'));
    }
    return this.http.get<BankAccount[] | null>(`${this.apiUrl}/${clientId}/accounts`, { headers: this.getHeaders() }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      map(accounts => accounts ?? []),
      catchError(this.handleError)
    );
  }

  getAccountBalance(clientId: string, accountId: string): Observable<number> {
    if (!clientId || !accountId) {
      return throwError(() => new Error('Client ID and Account ID are required'));
    }
    return this.getAccount(clientId, accountId).pipe(
      map(account => account.balance),
      catchError(this.handleError)
    );
  }
  bankAccountsList(): Observable<BankAccount[]> {
    const url = `${environment.apiUrl}/bankAccounts`;  // URL à adapter selon backend
    return this.http.get<BankAccount[]>(url, { headers: this.getHeaders() }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      catchError(this.handleError)
    );
  }
}

 */
