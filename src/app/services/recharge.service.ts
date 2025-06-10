import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { environment } from '../environments/environment';

// Interfaces pour les recharges téléphoniques
export interface PhoneRechargeRequest {
  phoneNumber: string;
  operatorCode: string;
  amount: number;
  transactionPin: string;
}

export interface PhoneRechargeResponse {
  status?: string;
  transactionReference?: string;
  amount?: number;
  phoneNumber?: string;
  message: string;
}

export interface Operator {
  code: string;
  name: string;
  logo: string;
  description: string;
  minAmount: number;
  maxAmount: number;
  currency: string;
}

export interface PhoneValidationRequest {
  phoneNumber: string;
  operatorCode: string;
}

export interface PhoneValidationResponse {
  valid: boolean;
  message: string;
  phoneNumber?: string;
  operatorCode?: string;
}

export interface AccountInfo {
  accountNumber: string;
  balance: number;
  accountType: string;
  rechargeProvider: string;
  supportedOperators: string[];
  maxRechargeAmount: number;
  currency: string;
}

export interface DailyLimits {
  dailyLimit: number;
  remainingLimit: number;
  transactionLimit: number;
  currency: string;
  mode: string;
  provider: string;
  note?: string;
}

export interface RechargeHistory {
  history: PhoneRecharge[];
  mode: string;
  provider: string;
  totalTransactions: number;
}

export interface PhoneRecharge {
  id?: number;
  phoneNumber: string;
  operatorCode: string;
  amount: number;
  clientAccountNumber: string;
  status: string;
  transactionReference?: string;
  createdAt?: string;
  processedAt?: string;
  failureReason?: string;
}

export interface ServiceStatus {
  reloadlyConnection: any;
  serviceAvailable: boolean;
  timestamp: number;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class RechargeService {
  private apiUrl = `${environment.apiUrl}/client/recharge`;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  /**
   * Obtient les headers HTTP avec authentification
   */
  private getAuthHeaders(): HttpHeaders {
    if (!isPlatformBrowser(this.platformId)) {
      return new HttpHeaders({ 'Content-Type': 'application/json' });
    }

    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('Token d\'authentification non trouvé');
    }

    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  /**
   * Effectue une recharge téléphonique
   */
  rechargePhone(request: PhoneRechargeRequest): Observable<PhoneRechargeResponse> {
    console.log('🔄 Processing phone recharge:', {
      phone: request.phoneNumber,
      operator: request.operatorCode,
      amount: request.amount
    });

    const headers = this.getAuthHeaders();

    return this.http.post<PhoneRechargeResponse>(
      `${this.apiUrl}/phone`,
      request,
      { headers }
    ).pipe(
      tap(response => {
        console.log('✅ Recharge response:', response);
        if (response.status === 'SUCCESS' || response.status === 'PENDING') {
          console.log(`💰 Recharge successful - Transaction: ${response.transactionReference}`);
        }
      }),
      catchError(err => {
        console.error('❌ Recharge failed:', err);
        const errorMessage = err.error?.message || err.error || 'Erreur lors de la recharge';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Obtient la liste des opérateurs supportés
   */
  getSupportedOperators(): Observable<Operator[]> {
    const headers = this.getAuthHeaders();

    return this.http.get<Operator[]>(`${this.apiUrl}/operators`, { headers }).pipe(
      tap(operators => {
        console.log('📱 Loaded operators:', operators.length);
      }),
      catchError(err => {
        console.error('❌ Error loading operators:', err);
        const errorMessage = err.error?.message || 'Erreur lors du chargement des opérateurs';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Obtient les montants prédéfinis pour les recharges
   */
  getPredefinedAmounts(): Observable<any> {
    const headers = this.getAuthHeaders();

    return this.http.get(`${this.apiUrl}/amounts`, { headers }).pipe(
      tap(amounts => {
        console.log('💰 Loaded predefined amounts:', amounts);
      }),
      catchError(err => {
        console.error('❌ Error loading amounts:', err);
        const errorMessage = err.error?.message || 'Erreur lors du chargement des montants';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Valide un numéro de téléphone selon l'opérateur
   */
  validatePhoneNumber(request: PhoneValidationRequest): Observable<PhoneValidationResponse> {
    const headers = this.getAuthHeaders();

    return this.http.post<PhoneValidationResponse>(
      `${this.apiUrl}/validate-phone`,
      request,
      { headers }
    ).pipe(
      tap(response => {
        console.log('📞 Phone validation result:', response);
      }),
      catchError(err => {
        console.error('❌ Phone validation error:', err);
        const errorMessage = err.error?.message || 'Erreur lors de la validation du numéro';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Obtient l'historique des recharges
   */
  getRechargeHistory(): Observable<RechargeHistory> {
    const headers = this.getAuthHeaders();

    return this.http.get<RechargeHistory>(`${this.apiUrl}/history`, { headers }).pipe(
      tap(history => {
        console.log('📋 Loaded recharge history:', history.totalTransactions, 'transactions');
      }),
      catchError(err => {
        console.error('❌ Error loading history:', err);
        const errorMessage = err.error?.message || 'Erreur lors du chargement de l\'historique';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Obtient les informations du compte
   */
  getAccountInfo(): Observable<AccountInfo> {
    const headers = this.getAuthHeaders();

    return this.http.get<AccountInfo>(`${this.apiUrl}/account-info`, { headers }).pipe(
      tap(info => {
        console.log('🏦 Account info loaded:', {
          balance: info.balance,
          currency: info.currency,
          provider: info.rechargeProvider
        });
      }),
      catchError(err => {
        console.error('❌ Error loading account info:', err);
        const errorMessage = err.error?.message || 'Erreur lors du chargement des informations du compte';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Obtient les limites quotidiennes
   */
  getDailyLimits(): Observable<DailyLimits> {
    const headers = this.getAuthHeaders();

    return this.http.get<DailyLimits>(`${this.apiUrl}/daily-limits`, { headers }).pipe(
      tap(limits => {
        console.log('📊 Daily limits loaded:', {
          daily: limits.dailyLimit,
          remaining: limits.remainingLimit,
          transaction: limits.transactionLimit
        });
      }),
      catchError(err => {
        console.error('❌ Error loading daily limits:', err);
        const errorMessage = err.error?.message || 'Erreur lors du chargement des limites';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Teste la connexion avec le service de recharge
   */
  testConnection(): Observable<any> {
    const headers = this.getAuthHeaders();

    return this.http.get(`${this.apiUrl}/test-connection`, { headers }).pipe(
      tap(result => {
        console.log('🔗 Connection test result:', result);
      }),
      catchError(err => {
        console.error('❌ Connection test failed:', err);
        const errorMessage = err.error?.message || 'Erreur de connexion au service de recharge';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Obtient le statut du service de recharge
   */
  getServiceStatus(): Observable<ServiceStatus> {
    const headers = this.getAuthHeaders();

    return this.http.get<ServiceStatus>(`${this.apiUrl}/service-status`, { headers }).pipe(
      tap(status => {
        console.log('📡 Service status:', {
          available: status.serviceAvailable,
          provider: 'Reloadly'
        });
      }),
      catchError(err => {
        console.error('❌ Error getting service status:', err);
        const errorMessage = err.error?.message || 'Erreur lors de la vérification du statut du service';
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Méthodes utilitaires pour la validation côté client
   */

  /**
   * Valide le format d'un numéro de téléphone marocain
   */
  isValidMoroccanPhoneNumber(phoneNumber: string, operatorCode?: string): boolean {
    if (!phoneNumber || phoneNumber.trim().length === 0) {
      return false;
    }

    // Nettoyer le numéro
    let cleanNumber = phoneNumber.replace(/[\s\-\(\)]/g, '');

    // Gérer les préfixes internationaux
    if (cleanNumber.startsWith('+212')) {
      cleanNumber = '0' + cleanNumber.substring(4);
    } else if (cleanNumber.startsWith('212')) {
      cleanNumber = '0' + cleanNumber.substring(3);
    }

    // Vérifier la longueur et le format de base
    if (cleanNumber.length !== 10 || !cleanNumber.startsWith('0')) {
      return false;
    }

    // Validation spécifique par opérateur si fourni
    if (operatorCode) {
      switch (operatorCode) {
        case 'IAM':
          return /^0(6[0-6]|7[0-1])\d{7}$/.test(cleanNumber);
        case 'ORANGE':
          return /^0(6[7-9])\d{7}$/.test(cleanNumber);
        case 'INWI':
          return /^0(6[5-6])\d{7}$/.test(cleanNumber);
        default:
          return /^0[67]\d{8}$/.test(cleanNumber);
      }
    }

    // Validation générale pour mobile marocain
    return /^0[67]\d{8}$/.test(cleanNumber);
  }

  /**
   * Détecte l'opérateur à partir du numéro de téléphone
   */
  detectOperatorFromNumber(phoneNumber: string): string | null {
    if (!this.isValidMoroccanPhoneNumber(phoneNumber)) {
      return null;
    }

    let cleanNumber = phoneNumber.replace(/[\s\-\(\)]/g, '');
    if (cleanNumber.startsWith('+212')) {
      cleanNumber = '0' + cleanNumber.substring(4);
    } else if (cleanNumber.startsWith('212')) {
      cleanNumber = '0' + cleanNumber.substring(3);
    }

    if (/^0(6[0-6]|7[0-1])\d{7}$/.test(cleanNumber)) {
      return 'IAM';
    } else if (/^0(6[7-9])\d{7}$/.test(cleanNumber)) {
      return 'ORANGE';
    } else if (/^0(6[5-6])\d{7}$/.test(cleanNumber)) {
      return 'INWI';
    }

    return null;
  }

  /**
   * Formate un numéro de téléphone pour l'affichage
   */
  formatPhoneNumber(phoneNumber: string): string {
    if (!phoneNumber) return '';

    let cleanNumber = phoneNumber.replace(/[\s\-\(\)]/g, '');
    if (cleanNumber.startsWith('+212')) {
      cleanNumber = '0' + cleanNumber.substring(4);
    } else if (cleanNumber.startsWith('212')) {
      cleanNumber = '0' + cleanNumber.substring(3);
    }

    if (cleanNumber.length === 10 && cleanNumber.startsWith('0')) {
      return `${cleanNumber.substring(0, 4)} ${cleanNumber.substring(4, 6)} ${cleanNumber.substring(6, 8)} ${cleanNumber.substring(8)}`;
    }

    return phoneNumber;
  }

  /**
   * Obtient l'icône de l'opérateur
   */
  getOperatorIcon(operatorCode: string): string {
    switch (operatorCode) {
      case 'IAM':
        return '/assets/iam-logo.png';
      case 'ORANGE':
        return '/assets/orange-logo.png';
      case 'INWI':
        return '/assets/inwi-logo.png';
      default:
        return '/assets/default-operator.png';
    }
  }

  /**
   * Obtient la couleur de l'opérateur
   */
  getOperatorColor(operatorCode: string): string {
    switch (operatorCode) {
      case 'IAM':
        return '#e74c3c';
      case 'ORANGE':
        return '#ff8c00';
      case 'INWI':
        return '#3498db';
      default:
        return '#95a5a6';
    }
  }

  /**
   * Debug: Obtient les informations de PIN (À supprimer en production!)
   */
  getDebugPinInfo(): Observable<any> {
    const headers = this.getAuthHeaders();

    return this.http.get(`${this.apiUrl}/debug-pin`, { headers }).pipe(
      tap(info => {
        console.warn('🚨 DEBUG: PIN Info loaded - REMOVE IN PRODUCTION!', info);
      }),
      catchError(err => {
        console.error('❌ Debug PIN error:', err);
        return throwError(() => new Error('Debug info not available'));
      })
    );
  }
}
