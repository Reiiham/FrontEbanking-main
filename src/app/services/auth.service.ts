import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { environment } from '../environments/environment';
import { LoginResponse } from '../model/login-response.model'; // Adjust the import path as necessary


@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  private clientIdSubject = new BehaviorSubject<string | null>(null);

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    if (isPlatformBrowser(this.platformId)) {
      this.isAuthenticatedSubject.next(!!localStorage.getItem('token'));
      this.clientIdSubject.next(localStorage.getItem('clientId'));
    }
  }

  // Méthode de login mise à jour pour gérer 2FA
  login(username: string, password: string): Observable<any> {
    return this.http.post<any>(
      `${this.apiUrl}/login`,
      { username, password },
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    ).pipe(
      tap(response => {
        // Si pas de 2FA requis, stocker le token directement
        if (response.token && !response.requires2FA) {
          if (isPlatformBrowser(this.platformId)) {
            localStorage.setItem('token', response.token);
            localStorage.setItem('role', response.role);
            localStorage.setItem('clientId', response.clientId);
            this.isAuthenticatedSubject.next(true);
            this.clientIdSubject.next(response.clientId);
          }
        }
      }),
      catchError(err => {
        const errorMessage = err.error?.message || err.error || 'Invalid credentials';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  // Nouvelle méthode pour vérifier le code 2FA
  verify2FA(username: string, pin: string): Observable<{ token: string, role: string, clientId: string, message: string }> {
    return this.http.post<{ token: string, role: string, clientId: string, message: string }>(
      `${this.apiUrl}/verify-2fa`,
      { username, pin },
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    ).pipe(
      tap(response => {
        if (isPlatformBrowser(this.platformId)) {
          localStorage.setItem('token', response.token);
          localStorage.setItem('role', response.role);
          localStorage.setItem('clientId', response.clientId);
          this.isAuthenticatedSubject.next(true);
          this.clientIdSubject.next(response.clientId);
        }
      }),
      catchError(err => {
        const errorMessage = err.error?.message || err.error || 'Code PIN invalide';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  // Nouvelle méthode pour renvoyer le code 2FA
  resend2FA(username: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.apiUrl}/resend-2fa`,
      { username },
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    ).pipe(
      catchError(err => {
        const errorMessage = err.error?.message || err.error || 'Erreur lors de l\'envoi du code';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  getClientProfile(): Observable<{ clientId: string }> {
    const token = isPlatformBrowser(this.platformId) ? localStorage.getItem('token') : null;
    if (!token) {
      return throwError(() => new Error('No token found'));
    }
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.get<{ clientId: string }>(`${this.apiUrl}/me`, { headers }).pipe(
      tap(response => {
        if (isPlatformBrowser(this.platformId)) {
          localStorage.setItem('clientId', response.clientId);
          this.clientIdSubject.next(response.clientId);
        }
      }),
      catchError(err => {
        const errorMessage = err.error?.message || 'Failed to fetch client profile';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  getRole(): Observable<string> {
    const token = isPlatformBrowser(this.platformId) ? localStorage.getItem('token') : null;
    const storedRole = isPlatformBrowser(this.platformId) ? localStorage.getItem('role') : null;
    if (!token) {
      return throwError(() => new Error('No token found'));
    }
    if (storedRole) {
      return new Observable(observer => {
        observer.next(storedRole);
        observer.complete();
      });
    }
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.get<{ role: string }>(`${this.apiUrl}/role`, { headers }).pipe(
      tap(response => {
        if (isPlatformBrowser(this.platformId)) {
          localStorage.setItem('role', response.role);
        }
      }),
      map(response => response.role),
      catchError(err => {
        const errorMessage = err.error?.message || 'Failed to fetch role';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  isLoggedIn(): boolean {
    return isPlatformBrowser(this.platformId) ? this.isAuthenticatedSubject.value : false;
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('token');
      localStorage.removeItem('role');
      localStorage.removeItem('clientId');
      this.isAuthenticatedSubject.next(false);
      this.clientIdSubject.next(null);
    }
  }

  getClientId(): string | null {
    return isPlatformBrowser(this.platformId) ? this.clientIdSubject.value : null;
  }
}
