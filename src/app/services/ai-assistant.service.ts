// Updated AIAssistantService with authentication support
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';

export interface AIResponse {
  responseText: string;
  success: boolean;
  intent?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AIAssistantService {
  private baseUrl = 'http://localhost:8090/eBankingVer1_war_exploded/api/clients';

  constructor(private http: HttpClient) {}

  // Method to get authentication headers
  private getAuthHeaders(): HttpHeaders {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });

    // Option 1: If you're using JWT token stored in localStorage
    const token = localStorage.getItem('authToken') || localStorage.getItem('token') || localStorage.getItem('jwt');
    if (token) {
      console.log('🔐 Adding JWT token to headers');
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    // Option 2: If you're using basic auth (uncomment if needed)
    // const username = 'your_username';
    // const password = 'your_password';
    // const basicAuth = btoa(`${username}:${password}`);
    // headers = headers.set('Authorization', `Basic ${basicAuth}`);

    // Option 3: If you're using session-based auth, make sure cookies are included
    // This is handled by withCredentials option below

    console.log('🔐 Final headers:', headers);
    return headers;
  }

  processMessage(clientId: string, message: string, language: string = 'fr'): Observable<AIResponse> {
    console.log('🚀 processMessage called with:', { clientId, message, language });

    const url = `${this.baseUrl}/${clientId}/assistant`;
    const params = new HttpParams().set('language', language);
    const headers = this.getAuthHeaders();

    console.log('📡 Making authenticated HTTP request to:', url);

    return this.http.post<AIResponse>(url, message, {
      params: params,
      headers: headers,
      withCredentials: true // Include cookies for session-based auth
    }).pipe(
      tap(response => {
        console.log('✅ Authenticated response received:', response);
      }),
      map(response => {
        const intent = this.extractIntentFromResponse(response.responseText);
        return {
          ...response,
          intent: intent || response.intent
        };
      }),
      catchError(error => {
        console.error('❌ Authentication error:', error);
        if (error.status === 401) {
          console.error('🔐 Authentication failed - check your credentials');
          // You might want to redirect to login page here
          return of({
            responseText: 'Session expirée. Veuillez vous reconnecter.',
            success: false,
            intent: undefined
          });
        }
        return of({
          responseText: `Erreur HTTP: ${error.status} - ${error.message}`,
          success: false,
          intent: undefined
        });
      })
    );
  }

  // Alternative method - check what authentication info you have available
  debugAuthInfo(): void {
    console.log('🔍 DEBUG: Checking available authentication info...');
    console.log('🔍 localStorage keys:', Object.keys(localStorage));
    console.log('🔍 sessionStorage keys:', Object.keys(sessionStorage));

    // Common token storage locations
    const possibleTokens = [
      'token', 'authToken', 'jwt', 'access_token', 'accessToken',
      'bearer_token', 'bearerToken', 'authorization', 'auth'
    ];

    possibleTokens.forEach(key => {
      const value = localStorage.getItem(key) || sessionStorage.getItem(key);
      if (value) {
        console.log(`🔐 Found token in ${key}:`, value.substring(0, 20) + '...');
      }
    });

    // Check cookies
    console.log('🍪 Document cookies:', document.cookie);
  }

  // Test method with different auth approaches
  testWithDifferentAuth(clientId: string, message: string): Observable<any> {
    const url = `${this.baseUrl}/${clientId}/assistant?language=fr`;

    // Test 1: With JWT from localStorage
    const token = localStorage.getItem('authToken') || localStorage.getItem('token');
    if (token) {
      console.log('🧪 Testing with JWT token...');
      const headers = new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      });

      return this.http.post(url, message, { headers }).pipe(
        tap(() => console.log('✅ JWT auth worked')),
        catchError(error => {
          console.log('❌ JWT auth failed:', error.status);
          return this.testWithCookies(url, message);
        })
      );
    }

    return this.testWithCookies(url, message);
  }

  private testWithCookies(url: string, message: string): Observable<any> {
    console.log('🧪 Testing with cookies/session...');
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });

    return this.http.post(url, message, {
      headers,
      withCredentials: true
    }).pipe(
      tap(() => console.log('✅ Cookie auth worked')),
      catchError(error => {
        console.log('❌ Cookie auth failed:', error.status);
        throw error;
      })
    );
  }

  private extractIntentFromResponse(responseText: string): string | undefined {
    if (!responseText) return undefined;

    const text = responseText.toLowerCase();
    if (text.includes('solde') || text.includes('balance')) return 'check_balance';
    if (text.includes('transaction') || text.includes('historique')) return 'get_transactions';
    if (text.includes('virement') || text.includes('transfer')) return 'transfer_money';
    if (text.includes('bénéficiaire') || text.includes('beneficiary')) return 'get_beneficiaries';
    if (text.includes('aide') || text.includes('help')) return 'help';
    if (text.includes('recharge')) return 'recharge_phone';

    return undefined;
  }

  // Other methods with auth headers
  getAccountBalance(clientId: string): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.baseUrl}/${clientId}/accounts`, {
      headers,
      withCredentials: true
    });
  }

  getTransactions(clientId: string, page: number = 0, size: number = 5): Observable<any> {
    const headers = this.getAuthHeaders();
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    return this.http.get<any>(`${this.baseUrl}/${clientId}/transactions`, {
      headers,
      params,
      withCredentials: true
    });
  }

  getBeneficiaries(clientId: string): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.baseUrl}/${clientId}/beneficiaries`, {
      headers,
      withCredentials: true
    });
  }
}
