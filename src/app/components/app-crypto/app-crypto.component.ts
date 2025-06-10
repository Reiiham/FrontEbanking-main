import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuthService } from '../../services/auth.service';

interface CryptoTransaction {
  id: string;
  symbol: string;
  amount: number;
  usdValue: number;
  type: string;
  date: string;
}

interface CryptoRate {
  symbol: string;
  rate: number;
}

interface PortfolioValue {
  portfolioUsdValue: number;
}

@Injectable({
  providedIn: 'root'
})
export class CryptoService {
  private apiUrl = `${environment.apiUrl}/crypto`;
  private readonly REQUEST_TIMEOUT = 10000;

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
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
    console.error('Crypto API error:', error);
    return throwError(() => new Error(errorMessage));
  }

  buyCrypto(symbol: string, usdAmount: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/buy`, null, {
      headers: this.getHeaders(),
      params: { symbol, usdAmount }
    }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      catchError(this.handleError)
    );
  }

  sellCrypto(symbol: string, quantity: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/sell`, null, {
      headers: this.getHeaders(),
      params: { symbol, quantity }
    }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      catchError(this.handleError)
    );
  }

  getPortfolioValue(): Observable<PortfolioValue> {
    return this.http.get<PortfolioValue>(`${this.apiUrl}/portfolio-value`, { headers: this.getHeaders() }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      catchError(this.handleError)
    );
  }

  getCryptoRates(): Observable<Map<string, number>> {
    return this.http.get<Map<string, number>>(`${this.apiUrl}/rates`, { headers: this.getHeaders() }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      catchError(this.handleError)
    );
  }

  getCryptoHistory(): Observable<CryptoTransaction[]> {
    return this.http.get<CryptoTransaction[]>(`${this.apiUrl}/history`, { headers: this.getHeaders() }).pipe(
      timeout(this.REQUEST_TIMEOUT),
      catchError(this.handleError)
    );
  }
}
