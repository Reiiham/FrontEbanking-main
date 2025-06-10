import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {AccountInfo, AccountResponse, TransferRequest, TransferResponse} from '../admin/transfer/transfer.component';
import {BankAccount} from '../admin/models/bank-account.model';
import {environment} from '../environments/environment';
import {catchError} from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class TransferService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('No token found');
      throw new Error('No token found');
    }
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  private handleError(error: any): Observable<never> {
    console.error('API Error:', error);
    return throwError(() => error);
  }

  transferFunds(request: TransferRequest): Observable<TransferResponse> {
    return this.http.post<TransferResponse>(
      `${this.apiUrl}/funds`,
      request,
      { headers: this.getHeaders() }
    ).pipe(
      catchError(this.handleError)
    );
  }

  getAccountByRib(rib: string): Observable<AccountResponse> {
    return this.http.get<AccountResponse>(
      `${this.apiUrl}/account/${rib}`,
      { headers: this.getHeaders() }
    ).pipe(
      catchError(this.handleError)
    );
  }

  getAllAccounts(): Observable<AccountInfo[]> {
    return this.http.get<AccountInfo[]>(
      `${this.apiUrl}/accounts`,
      { headers: this.getHeaders() }
    ).pipe(
      catchError(this.handleError)
    );
  }
}
