// import { Injectable } from '@angular/core';
// import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
// import {Observable, throwError} from 'rxjs';
// import { ActivateServicesRequest } from '../banque/models/activate-services-request.model';
// import { SuspendServicesRequest } from '../banque/models/suspend-services-request.model';
// import { environment } from '../environments/environment';
// import {catchError, tap} from 'rxjs/operators';
//
//
// @Injectable({ providedIn: 'root' })
// export class ServiceManagerService {
//   private baseUrl = `${environment.apiUrl}/employee`;
//
//   constructor(private http: HttpClient) {}
//
//   // Helper method to get auth headers - FIXED to use same key as AuthService
//   private getAuthHeaders(): HttpHeaders {
//     const token = localStorage.getItem('token'); // Only check 'token' key
//     console.log('🔑 ServiceManager Token found:', token ? 'YES' : 'NO');
//
//     let headers = new HttpHeaders({
//       'Content-Type': 'application/json'
//     });
//
//     if (token) {
//       headers = headers.set('Authorization', `Bearer ${token}`);
//     }
//
//     return headers;
//   }
//
//   // Helper method for error handling
//   private handleError = (operation = 'operation') => {
//     return (error: HttpErrorResponse): Observable<never> => {
//       console.error(`❌ ServiceManager ${operation} failed:`, error);
//
//       if (error.status === 401) {
//         console.error('🚨 UNAUTHORIZED - Token might be invalid');
//         localStorage.removeItem('token');
//       }
//
//       return throwError(() => error);
//     };
//   };
//
//   activateServices(data: ActivateServicesRequest): Observable<any> {
//     console.log('🔧 Calling activateServices() with data:', data);
//     const url = `${this.baseUrl}/clients/activer-services`;
//     console.log('🌐 URL:', url);
//
//     return this.http.post(url, data, {
//       headers: this.getAuthHeaders()
//     }).pipe(
//       tap(result => console.log('✅ Activate services result:', result)),
//       catchError(this.handleError('activateServices'))
//     );
//   }
//
//   suspendServices(clientId: string, data: SuspendServicesRequest): Observable<any> {
//     console.log('⏸️ Calling suspendServices() for client:', clientId, 'with data:', data);
//     const url = `${this.baseUrl}/clients/${clientId}/suspend-services`;
//     console.log('🌐 URL:', url);
//
//     return this.http.put(url, data, {
//       headers: this.getAuthHeaders()
//     }).pipe(
//       tap(result => console.log('✅ Suspend services result:', result)),
//       catchError(this.handleError('suspendServices'))
//     );
//   }
//
//   reactivateServices(clientId: string, services: string[]): Observable<any> {
//     console.log('▶️ Calling reactivateServices() for client:', clientId, 'services:', services);
//     const url = `${this.baseUrl}/clients/${clientId}/reactivate-services`;
//     console.log('🌐 URL:', url);
//
//     return this.http.put(url, services, {
//       headers: this.getAuthHeaders()
//     }).pipe(
//       tap(result => console.log('✅ Reactivate services result:', result)),
//       catchError(this.handleError('reactivateServices'))
//     );
//   }
// }

import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import { ActivateServicesRequest } from '../banque/models/activate-services-request.model';
import { SuspendServicesRequest } from '../banque/models/suspend-services-request.model';
import { environment } from '../environments/environment';
import {catchError, tap} from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class ServiceManagerService {
  private baseUrl = `${environment.apiUrl}/employee`;

  constructor(private http: HttpClient) {}

  // Helper method to get auth headers - FIXED to use same keys as component
  private getAuthHeaders(): HttpHeaders {
    // Vérifier les deux clés comme dans le component
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken') || localStorage.getItem('token');
    console.log('🔑 ServiceManager Token found:', token ? 'YES' : 'NO');
    console.log('🔑 Token from localStorage authToken:', localStorage.getItem('authToken') ? 'YES' : 'NO');
    console.log('🔑 Token from sessionStorage authToken:', sessionStorage.getItem('authToken') ? 'YES' : 'NO');
    console.log('🔑 Token from localStorage token:', localStorage.getItem('token') ? 'YES' : 'NO');

    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
      console.log('🔑 Authorization header set with token');
    } else {
      console.error('🚨 NO TOKEN FOUND - Request will fail');
    }

    return headers;
  }

  // Helper method for error handling
  private handleError = (operation = 'operation') => {
    return (error: HttpErrorResponse): Observable<never> => {
      console.error(`❌ ServiceManager ${operation} failed:`, error);
      console.error(`❌ Error status: ${error.status}`);
      console.error(`❌ Error message: ${error.message}`);
      console.error(`❌ Error body:`, error.error);

      if (error.status === 401) {
        console.error('🚨 UNAUTHORIZED - Token might be invalid');
        // Nettoyer tous les tokens
        localStorage.removeItem('token');
        localStorage.removeItem('authToken');
        sessionStorage.removeItem('authToken');
      }

      return throwError(() => error);
    };
  };

  activateServices(data: ActivateServicesRequest): Observable<any> {
    console.log('🔧 Calling activateServices() with data:', data);
    console.log('🔧 Data type:', typeof data);
    console.log('🔧 Data JSON:', JSON.stringify(data, null, 2));

    const url = `${this.baseUrl}/clients/activer-services`;
    console.log('🌐 URL:', url);

    const headers = this.getAuthHeaders();
    console.log('🌐 Headers:', headers.keys());

    // Spécifier que la réponse est du texte brut
    return this.http.post(url, data, {
      headers,
      responseType: 'text' // Important: spécifier que la réponse est du texte
    }).pipe(
      tap(result => {
        console.log('✅ Activate services SUCCESS:', result);
        console.log('✅ Result type:', typeof result);
      }),
      catchError(this.handleError('activateServices'))
    );
  }

  suspendServices(clientId: string, data: SuspendServicesRequest): Observable<any> {
    console.log('⏸️ Calling suspendServices() for client:', clientId, 'with data:', data);
    const url = `${this.baseUrl}/clients/${clientId}/suspend-services`;
    console.log('🌐 URL:', url);

    return this.http.put(url, data, {
      headers: this.getAuthHeaders(),
      responseType: 'text' // Spécifier que la réponse est du texte
    }).pipe(
      tap(result => console.log('✅ Suspend services result:', result)),
      catchError(this.handleError('suspendServices'))
    );
  }

  reactivateServices(clientId: string, services: string[]): Observable<any> {
    console.log('▶️ Calling reactivateServices() for client:', clientId, 'services:', services);
    const url = `${this.baseUrl}/clients/${clientId}/reactivate-services`;
    console.log('🌐 URL:', url);

    return this.http.put(url, services, {
      headers: this.getAuthHeaders(),
      responseType: 'text' // Spécifier que la réponse est du texte
    }).pipe(
      tap(result => console.log('✅ Reactivate services result:', result)),
      catchError(this.handleError('reactivateServices'))
    );
  }
}
