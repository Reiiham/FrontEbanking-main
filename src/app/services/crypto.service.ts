import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

// Interfaces pour les réponses API
export interface CryptoTransaction {
  id: string;
  userId: number;
  symbol: string;
  side: string; // 'BUY' ou 'SELL'
  quantity: number;
  price: number;
  timestamp: string;
}

export interface CryptoRate {
  symbol: string;
  rate: number;
}

export interface CryptoBalance {
  [currency: string]: number;
}

export interface CryptoStats {
  symbol: string;
  totalQuantity: number;
  averagePrice: number;
  currentValue: number;
  profitLoss: number;
}

export interface PortfolioValue {
  portfolioUsdValue: number;
}

export interface DepositAddress {
  currency: string;
  address: string;
}

export interface ApiResponse {
  message?: string;
  error?: string;
  binanceResponse?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CryptoService {
  private apiUrl = `${environment.apiUrl}/crypto`;

  // BehaviorSubjects pour le state management
  private portfolioValueSubject = new BehaviorSubject<number>(0);
  private balancesSubject = new BehaviorSubject<CryptoBalance>({});
  private ratesSubject = new BehaviorSubject<{[key: string]: number}>({});

  constructor(private http: HttpClient) {}

  /**
   * Obtenir les headers avec authentification
   */
  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  // ===============================
  // TRADING OPERATIONS
  // ===============================

  /**
   * Acheter des cryptomonnaies
   */
  buyCrypto(symbol: string, usdAmount: number): Observable<ApiResponse> {
    const params = new HttpParams()
      .set('symbol', symbol)
      .set('usdAmount', usdAmount.toString());

    return this.http.post<ApiResponse>(`${this.apiUrl}/buy`, null, {
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(
      tap(() => {
        // Rafraîchir le portfolio après achat
        this.refreshPortfolioData();
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Vendre des cryptomonnaies
   */
  sellCrypto(symbol: string, quantity: number): Observable<ApiResponse> {
    const params = new HttpParams()
      .set('symbol', symbol)
      .set('quantity', quantity.toString());

    return this.http.post<ApiResponse>(`${this.apiUrl}/sell`, null, {
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(
      tap(() => {
        // Rafraîchir le portfolio après vente
        this.refreshPortfolioData();
      }),
      catchError(this.handleError)
    );
  }

  // ===============================
  // PORTFOLIO & BALANCE
  // ===============================

  /**
   * Obtenir la valeur totale du portfolio
   */
  getPortfolioValue(): Observable<PortfolioValue> {
    return this.http.get<PortfolioValue>(`${this.apiUrl}/portfolio-value`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(response => {
        this.portfolioValueSubject.next(response.portfolioUsdValue);
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Obtenir les statistiques crypto
   */
  getCryptoStats(): Observable<CryptoStats[]> {
    return this.http.get<CryptoStats[]>(`${this.apiUrl}/stats`, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Obtenir les soldes crypto
   */
  getCryptoBalance(currency?: string): Observable<CryptoBalance> {
    let params = new HttpParams();
    if (currency) {
      params = params.set('currency', currency);
    }

    return this.http.get<CryptoBalance>(`${this.apiUrl}/balance`, {
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(
      tap(balances => {
        this.balancesSubject.next(balances);
      }),
      catchError(this.handleError)
    );
  }

  // ===============================
  // RATES & MARKET DATA
  // ===============================

  /**
   * Obtenir le taux d'une cryptomonnaie
   */
  getRate(symbol: string): Observable<CryptoRate> {
    return this.http.get<CryptoRate>(`${this.apiUrl}/rate/${symbol}`, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Obtenir tous les taux
   */
  getAllRates(): Observable<{[key: string]: number}> {
    return this.http.get<{[key: string]: number}>(`${this.apiUrl}/rates`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(rates => {
        this.ratesSubject.next(rates);
      }),
      catchError(this.handleError)
    );
  }

  // ===============================
  // TRANSACTION HISTORY
  // ===============================

  /**
   * Obtenir l'historique des transactions
   */
  getTransactionHistory(): Observable<CryptoTransaction[]> {
    return this.http.get<CryptoTransaction[]>(`${this.apiUrl}/history`, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Rechercher des transactions avec filtres
   */
  searchTransactions(currency?: string, startDate?: string, endDate?: string): Observable<any[]> {
    let params = new HttpParams();

    if (currency) {
      params = params.set('currency', currency);
    }
    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }

    return this.http.get<any[]>(`${this.apiUrl}/search`, {
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(
      catchError(this.handleError)
    );
  }

  // ===============================
  // TESTNET OPERATIONS
  // ===============================

  /**
   * Obtenir l'adresse de dépôt Binance Testnet
   */
  getTestnetDepositAddress(currency: string): Observable<DepositAddress> {
    const params = new HttpParams().set('currency', currency);

    return this.http.get<DepositAddress>(`${this.apiUrl}/testnet/address`, {
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(
      catchError(this.handleError)
    );
  }

  // ===============================
  // OBSERVABLES FOR STATE MANAGEMENT
  // ===============================

  /**
   * Observable pour la valeur du portfolio
   */
  get portfolioValue$(): Observable<number> {
    return this.portfolioValueSubject.asObservable();
  }

  /**
   * Observable pour les balances crypto
   */
  get balances$(): Observable<CryptoBalance> {
    return this.balancesSubject.asObservable();
  }

  /**
   * Observable pour les taux de change
   */
  get rates$(): Observable<{[key: string]: number}> {
    return this.ratesSubject.asObservable();
  }

  // ===============================
  // UTILITY METHODS
  // ===============================

  /**
   * Rafraîchir toutes les données du portfolio
   */
  refreshPortfolioData(): void {
    this.getPortfolioValue().subscribe();
    this.getCryptoBalance().subscribe();
    this.getAllRates().subscribe();
  }

  /**
   * Formater les symboles crypto (ajouter USDT si nécessaire)
   */
  formatSymbol(symbol: string): string {
    if (!symbol.endsWith('USDT')) {
      return symbol + 'USDT';
    }
    return symbol;
  }

  /**
   * Calculer le profit/perte en pourcentage
   */
  calculateProfitLossPercentage(buyPrice: number, currentPrice: number): number {
    return ((currentPrice - buyPrice) / buyPrice) * 100;
  }

  /**
   * Vérifier si un symbole est supporté
   */
  isSupportedSymbol(symbol: string): boolean {
    const supportedSymbols = ['BTCUSDT', 'ETHUSDT'];
    return supportedSymbols.includes(this.formatSymbol(symbol));
  }

  /**
   * Obtenir la liste des symboles supportés
   */
  getSupportedSymbols(): string[] {
    return ['BTCUSDT', 'ETHUSDT'];
  }

  /**
   * Formater le montant en USD
   */
  formatUSD(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  /**
   * Formater la quantité crypto
   */
  formatCryptoAmount(amount: number, decimals: number = 8): string {
    return amount.toFixed(decimals);
  }

  /**
   * Gestion centralisée des erreurs
   */
  private handleError = (error: any): Observable<never> => {
    console.error('CryptoService Error:', error);

    let errorMessage = 'Une erreur est survenue';

    if (error.error?.error) {
      errorMessage = error.error.error;
    } else if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    }

    return throwError(() => new Error(errorMessage));
  };

  // ===============================
  // REAL-TIME DATA (Future enhancement)
  // ===============================

  /**
   * Démarrer la mise à jour automatique des taux
   */
  startRealTimeRates(intervalMs: number = 10000): void {
    setInterval(() => {
      this.getAllRates().subscribe({
        error: (err) => console.warn('Failed to update rates:', err)
      });
    }, intervalMs);
  }

  /**
   * Obtenir les données de marché étendues (Future enhancement)
   */
  getMarketData(symbol: string): Observable<any> {
    // Cette méthode pourrait être étendue pour récupérer plus de données de marché
    // comme les volumes, les variations 24h, etc.
    return this.getRate(symbol);
  }
}
