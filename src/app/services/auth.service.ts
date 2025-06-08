import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {Observable, throwError, BehaviorSubject, of} from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { environment } from '../environments/environment';
import { LoginResponse } from '../model/login-response.model'; // Adjust the import path as necessary

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
      const token = localStorage.getItem('token');
      const isValid = token && !this.isTokenExpired(token);

      if (typeof isValid === "boolean") {
        this.isAuthenticatedSubject.next(isValid);
      }
      if (isValid) {
        this.clientIdSubject.next(localStorage.getItem('clientId'));
      } else if (token) {
        // Token expiré, nettoyer le localStorage
        this.clearTokens();
      }
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
          this.storeTokens(response);
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
        this.storeTokens(response);
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
   * Stockage centralisé des tokens
   */
  private storeTokens(response: any): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('token', response.token);
      localStorage.setItem('role', response.role);
      localStorage.setItem('clientId', response.clientId);

      // Ajouter un timestamp pour le debugging
      localStorage.setItem('tokenTimestamp', Date.now().toString());

      this.isAuthenticatedSubject.next(true);
      this.clientIdSubject.next(response.clientId);

      console.log('✅ Tokens stored successfully');
      console.log('   Token length:', response.token?.length);
      console.log('   Role:', response.role);
      console.log('   ClientId:', response.clientId);
    }
  }

  /**
   * Nettoyage centralisé des tokens
   */
  private clearTokens(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('token');
      localStorage.removeItem('role');
      localStorage.removeItem('clientId');
      localStorage.removeItem('tokenTimestamp');

      this.isAuthenticatedSubject.next(false);
      this.clientIdSubject.next(null);

      console.log('🧹 Tokens cleared');
    }
  }

  /**
   * Vérification si le token JWT est expiré
   */
  private isTokenExpired(token: string): boolean {
    try {
      // Vérifier si c'est un JWT
      const parts = token.split('.');
      if (parts.length !== 3) {
        console.log('⚠️ Token is not a JWT format');
        return false; // Pas un JWT, on ne peut pas vérifier l'expiration
      }

      const payload = JSON.parse(atob(parts[1]));

      if (!payload.exp) {
        console.log('⚠️ Token has no expiration date');
        return false; // Pas d'expiration définie
      }

      const currentTime = Math.floor(Date.now() / 1000);
      const isExpired = payload.exp < currentTime;

      if (isExpired) {
        console.log('🚨 Token is expired');
        console.log('   Expired at:', new Date(payload.exp * 1000));
        console.log('   Current time:', new Date(currentTime * 1000));
      } else {
        const timeToExpiry = payload.exp - currentTime;
        console.log(`⏱️ Token expires in ${Math.floor(timeToExpiry / 60)} minutes`);
      }

      return isExpired;
    } catch (error) {
      console.error('❌ Error parsing token:', error);
      return true; // Considérer comme expiré si on ne peut pas le parser
    }
  }

  /**
   * Validation du token auprès du serveur
   */


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
    const token = this.getToken();
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
    const token = this.getToken();
    const storedRole = this.getStoredRole();

    if (!token) {
      return throwError(() => new Error('No token found'));
    }

    if (storedRole) {
      return of(storedRole);
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

  /**
   * Vérification améliorée de l'état de connexion
   */
  isLoggedIn(): boolean {
    if (!isPlatformBrowser(this.platformId)) {
      return false;
    }

    const token = localStorage.getItem('token');
    if (!token) {
      console.log('🚨 No token found in localStorage');
      return false;
    }

    // Vérification de l'expiration pour les JWT
    if (this.isTokenExpired(token)) {
      console.log('🚨 Token is expired, cleaning up');
      this.clearTokens();
      return false;
    }

    console.log('✅ User is logged in with valid token');
    return true;
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
    console.log('🚪 Logging out user');
    this.clearTokens();
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

  /**
   * Debug: Afficher les informations de session
   */
  debugSessionInfo(): void {
    if (!isPlatformBrowser(this.platformId)) {
      console.log('🚨 Not in browser environment');
      return;
    }

    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    const clientId = localStorage.getItem('clientId');
    const timestamp = localStorage.getItem('tokenTimestamp');

    console.log('🔍 Session Debug Info:');
    console.log('   Token exists:', !!token);
    console.log('   Token length:', token?.length || 0);
    console.log('   Role:', role);
    console.log('   ClientId:', clientId);
    console.log('   Token stored at:', timestamp ? new Date(parseInt(timestamp)) : 'Unknown');
    console.log('   Is logged in:', this.isLoggedIn());

    if (token) {
      console.log('   Token expired:', this.isTokenExpired(token));
    }
  }
}
