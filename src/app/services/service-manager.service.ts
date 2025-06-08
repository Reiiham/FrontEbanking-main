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

  // Helper method to get auth headers
  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token') || localStorage.getItem('authToken') || localStorage.getItem('jwt');
    console.log('🔑 ServiceManager Token found:', token ? 'YES' : 'NO');

    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    return headers;
  }

  // Helper method for error handling
  private handleError = (operation = 'operation') => {
    return (error: HttpErrorResponse): Observable<never> => {
      console.error(`❌ ServiceManager ${operation} failed:`, error);
      return throwError(() => error);
    };
  };

  activateServices(data: ActivateServicesRequest): Observable<any> {
    console.log('🔧 Calling activateServices() with data:', data);
    const url = `${this.baseUrl}/clients/activer-services`;
    console.log('🌐 URL:', url);

    return this.http.post(url, data, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Activate services result:', result)),
      catchError(this.handleError('activateServices'))
    );
  }

  suspendServices(clientId: string, data: SuspendServicesRequest): Observable<any> {
    console.log('⏸️ Calling suspendServices() for client:', clientId, 'with data:', data);
    const url = `${this.baseUrl}/clients/${clientId}/suspend-services`;
    console.log('🌐 URL:', url);

    return this.http.put(url, data, {
      headers: this.getAuthHeaders()
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
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Reactivate services result:', result)),
      catchError(this.handleError('reactivateServices'))
    );
  }
}

