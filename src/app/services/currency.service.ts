import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Currency } from '../admin/models/currency.model';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CurrencyService {
  private apiUrl = `${environment.apiUrl}/admin/currencies`;

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

  getAllCurrencies(): Observable<Currency[]> {
    return this.http.get<Currency[]>(this.apiUrl, { headers: this.getHeaders() }).pipe(
      catchError(err => {
        console.error('Error fetching currencies:', err);
        return throwError(() => new Error('Failed to fetch currencies: ' + (err.error?.message || err.message)));
      })
    );
  }

  addCurrency(currency: Currency): Observable<Currency> {
    return this.http.post<Currency>(this.apiUrl, currency, { headers: this.getHeaders() }).pipe(
      catchError(err => {
        console.error('Error adding currency:', err);
        return throwError(() => new Error('Failed to add currency: ' + (err.error?.message || err.message)));
      })
    );
  }

  updateCurrency(id: number, currency: Currency): Observable<Currency> {
    return this.http.put<Currency>(`${this.apiUrl}/${id}`, currency, { headers: this.getHeaders() }).pipe(
      catchError(err => {
        console.error('Error updating currency:', err);
        return throwError(() => new Error('Failed to update currency: ' + (err.error?.message || err.message)));
      })
    );
  }

  deleteCurrency(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() }).pipe(
      catchError(err => {
        console.error('Error deleting currency:', err);
        return throwError(() => new Error('Failed to delete currency: ' + (err.error?.message || err.message)));
      })
    );
  }
}
