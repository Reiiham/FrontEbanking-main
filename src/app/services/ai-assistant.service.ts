// Updated AIAssistantService with authentication support
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { switchMap } from 'rxjs/operators';

export interface AIResponse {
  responseText: string;
  success: boolean;
  intent?: string;
}

// Backend response interface to match your API
interface BackendAssistantResponse {
  success: boolean;
  response: string;
  intent?: string;
  language: string;
}

@Injectable({
  providedIn: 'root'
})
export class AIAssistantService {
  // FIX: Corrected the URL to match your working Postman URL
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

    console.log('🔐 Final headers:', headers.keys());
    return headers;
  }

  // FIX: Changed from GET to POST request and updated to match the working processMessage pattern
  getAccountBalance(clientId: string): Observable<any[]> {
    console.log('🔍 Getting account balance for client:', clientId);

    const headers = this.getAuthHeaders();
    const url = `${this.baseUrl}/${clientId}/assistant`;

    // Create request body for balance check
    const requestBody = {
      request: "Check my account balance"  // This matches your working Postman request
    };

    // Debug logging
    console.log('📡 Full URL:', url);
    console.log('🔐 Headers being sent:', headers);
    console.log('📡 Request body:', requestBody);

    // FIX: Changed from http.get to http.post with request body
    return this.http.post<BackendAssistantResponse>(url, requestBody, {
      headers,
      withCredentials: true
    }).pipe(
      tap(response => {
        console.log('✅ Balance response:', response);
      }),
      map(response => {
        // Parse the response to extract account information
        // You may need to adjust this based on your actual backend response format
        if (response.success && response.response) {
          try {
            // If the response contains account data, parse it
            // This is a placeholder - adjust based on your actual response structure
            return this.parseAccountBalanceFromResponse(response.response);
          } catch (error) {
            console.warn('Could not parse account data from response, returning empty array');
            return [];
          }
        }
        return [];
      }),
      catchError(error => {
        console.error('❌ Complete error object:', error);
        console.error('❌ Error URL:', error.url);
        console.error('❌ Error status:', error.status);
        console.error('❌ Error headers:', error.headers);
        console.error('❌ Error body type:', typeof error.error);
        console.error('❌ Error body content:', error.error);

        // If it's HTML response instead of JSON
        if (typeof error.error === 'string' && error.error.includes('<!doctype')) {
          console.error('🚨 Backend returned HTML instead of JSON - likely 404 or auth redirect');
        }

        return of([]);
      })
    );
  }

  // Helper method to parse account balance from AI response
  private parseAccountBalanceFromResponse(responseText: string): any[] {
    // This is a placeholder method - you'll need to implement based on how your AI returns account data
    // For now, return empty array and let the component handle the AI response text
    console.log('Parsing account balance from:', responseText);
    return [];
  }

  processMessage(clientId: string, message: string, language: string = 'fr'): Observable<AIResponse> {
    console.log('🚀 processMessage called with:', { clientId, message, language });

    const url = `${this.baseUrl}/${clientId}/assistant`;
    const headers = this.getAuthHeaders();

    // Create request body that matches your backend expectation
    const requestBody = {
      request: message  // Backend expects "request" key, not the message directly
    };

    // Create query parameters
    const params = new HttpParams().set('language', language);

    console.log('📡 Making authenticated HTTP request to:', url);
    console.log('📡 With body:', requestBody);
    console.log('📡 With params:', params.toString());

    return this.http.post<BackendAssistantResponse>(url, requestBody, {
      params: params,
      headers: headers,
      withCredentials: true
    }).pipe(
      tap(response => {
        console.log('✅ Backend response received:', response);
      }),
      map(backendResponse => {
        // Transform backend response to match frontend interface
        const transformedResponse: AIResponse = {
          responseText: backendResponse.response,
          success: backendResponse.success,
          intent: backendResponse.intent
        };

        console.log('🔄 Transformed response:', transformedResponse);
        return transformedResponse;
      }),
      catchError(error => {
        console.error('❌ API error:', error);
        console.error('❌ Error status:', error.status);
        console.error('❌ Error body:', error.error);

        if (error.status === 401) {
          console.error('🔐 Authentication failed - check your credentials');
          return of({
            responseText: 'Session expirée. Veuillez vous reconnecter.',
            success: false,
            intent: undefined
          });
        } else if (error.status === 403) {
          // Handle forbidden access or service not activated
          const errorMessage = error.error?.error || 'Accès refusé';
          console.error('🚫 Forbidden access:', errorMessage);
          return of({
            responseText: errorMessage,
            success: false,
            intent: undefined
          });
        } else if (error.status === 400) {
          // Handle bad request
          const errorMessage = error.error?.error || 'Requête invalide';
          console.error('❌ Bad request:', errorMessage);
          return of({
            responseText: errorMessage,
            success: false,
            intent: undefined
          });
        } else if (error.status === 404) {
          console.error('🔍 Endpoint not found - check URL mapping');
          return of({
            responseText: 'Service temporairement indisponible.',
            success: false,
            intent: undefined
          });
        } else if (error.status === 405) {
          // Method Not Allowed - this was your main issue
          console.error('🚫 Method not allowed - check if endpoint expects POST');
          return of({
            responseText: 'Erreur de configuration du service.',
            success: false,
            intent: undefined
          });
        } else if (error.status === 500) {
          // Handle server error
          const errorMessage = error.error?.error || 'Erreur serveur interne';
          console.error('💥 Server error:', errorMessage);
          return of({
            responseText: `Erreur serveur: ${errorMessage}`,
            success: false,
            intent: undefined
          });
        }

        return of({
          responseText: `Erreur de communication avec le serveur (${error.status}).`,
          success: false,
          intent: undefined
        });
      })
    );
  }

  // Check if assistant service is available and healthy
  checkAssistantHealth(): Observable<{modelAvailable: boolean}> {
    const url = `${this.baseUrl}/assistant/health`;
    const headers = this.getAuthHeaders();

    return this.http.get<{modelAvailable: boolean}>(url, {
      headers,
      withCredentials: true
    }).pipe(
      tap(response => {
        console.log('🏥 Assistant health check:', response);
      }),
      catchError(error => {
        console.error('❌ Health check failed:', error);
        return of({ modelAvailable: false });
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
  testConnection(clientId: string, message: string = "test"): Observable<any> {
    console.log('🧪 Testing connection to assistant service...');

    // First check health
    return this.checkAssistantHealth().pipe(
      switchMap(healthResponse => {
        if (!healthResponse.modelAvailable) {
          console.warn('⚠️ AI model not available');
        }

        // Then test actual message processing
        return this.processMessage(clientId, message, 'fr');
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

  getTransactions(clientId: string, page: number = 0, size: number = 5): Observable<any> {
    const headers = this.getAuthHeaders();
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

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
