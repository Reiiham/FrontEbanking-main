import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { environment } from '../environments/environment';
import { LoginResponse } from '../model/login-response.model'; // Adjust the import path as necessary
import {jwtDecode} from 'jwt-decode';

// Interfaces pour les nouvelles fonctionnalités
export interface SetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface TokenValidationResponse {
  valid: boolean;
  message: string;
}

export interface ApiResponse {
  message: string;
  error?: string;
}

export interface ResendActivationRequest {
  email: string;
}

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
            console.log('Token sauvegardé', response.token);
          }
        }
      }),
      catchError(err => {
        const errorMessage = err.error?.message || err.error || 'Invalid credentials';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  // Méthode pour vérifier le code 2FA
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

  // Méthode pour renvoyer le code 2FA
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

  /**
   * Valide un token de réinitialisation de mot de passe
   */
  validateToken(token: string): Observable<TokenValidationResponse> {
    return this.http.get<TokenValidationResponse>(`${this.apiUrl}/validate-token/${token}`);
  }

  /**
   * Définit un nouveau mot de passe avec un token
   */
  setPassword(request: SetPasswordRequest): Observable<ApiResponse> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    return this.http.post<ApiResponse>(`${this.apiUrl}/set-password`, request, { headers });
  }

  /**
   * Renvoie un lien d'activation par email
   */
  resendActivationLink(request: ResendActivationRequest): Observable<ApiResponse> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    return this.http.post<ApiResponse>(`${this.apiUrl}/resend-activation`, request, { headers });
  }

  // Obtenir le profil client
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

  // Obtenir le rôle utilisateur
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

  // Vérifier si l'utilisateur est connecté
//   isLoggedIn(): boolean {
//
//     if (isPlatformBrowser(this.platformId)) {
//     const token = localStorage.getItem('token');
//     return !!token; // retourne true si token existe, false sinon
//   }
//   return false;
// }

  isLoggedIn(): boolean {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('token');
      console.log('🔍 isLoggedIn check - Token exists:', !!token);

      if (!token) {
        console.log('❌ No token found');
        return false;
      }

      try {
        const decoded: any = jwtDecode(token);
        const exp = decoded.exp * 1000; // JWT exp est en secondes
        const now = Date.now();

        console.log('🔍 Token decoded successfully');
        console.log('🕐 Token expires at:', new Date(exp));
        console.log('🕐 Current time:', new Date(now));
        console.log('🔍 Token valid:', now < exp);

        return now < exp;
      } catch (e) {
        console.error('❌ Invalid token:', e);
        console.log('🔍 Token content (first 50 chars):', token.substring(0, 50));
        return false;
      }
    }
    console.log('❌ Not in browser platform');
    return false;
  }





  // Observable pour l'état d'authentification
  get isAuthenticated$(): Observable<boolean> {
    return this.isAuthenticatedSubject.asObservable();
  }

  // Observable pour le clientId
  get clientId$(): Observable<string | null> {
    return this.clientIdSubject.asObservable();
  }

  // Déconnexion
  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('token');
      localStorage.removeItem('role');
      localStorage.removeItem('clientId');
      this.isAuthenticatedSubject.next(false);
      this.clientIdSubject.next(null);
    }
  }

  // Obtenir le clientId
  getClientId(): string | null {
    return isPlatformBrowser(this.platformId) ? this.clientIdSubject.value : null;
  }

  // Obtenir le token stocké
  getToken(): string | null {
    return isPlatformBrowser(this.platformId) ? localStorage.getItem('token') : null;
  }

  // Obtenir le rôle stocké
  getStoredRole(): string | null {
    return isPlatformBrowser(this.platformId) ? localStorage.getItem('role') : null;
  }
}
