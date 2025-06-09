import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import { ClientBasicDTO } from '../banque/models/client-basic.model';
import { ClientSummaryDTO } from '../banque/models/client-summary.model';
import { ClientUpdateRequest } from '../banque/models/client-update-request.model';
import { environment } from '../environments/environment';
import {catchError, tap} from 'rxjs/operators';


@Injectable({ providedIn: 'root' })
export class ClientService {
  private baseUrl = `${environment.apiUrl}/employee`;

  constructor(private http: HttpClient) {}

  // Helper method to get auth headers
  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token') || localStorage.getItem('authToken') || localStorage.getItem('jwt');
    console.log('🔑 Token found:', token ? 'YES' : 'NO');
    console.log('🔑 Token value:', token?.substring(0, 20) + '...');

    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    if (token) {
      // Try different auth header formats
      headers = headers.set('Authorization', `Bearer ${token}`);
      // Some backends expect just the token
      // headers = headers.set('Authorization', token);
    } else {
      console.warn('⚠️ No auth token found in localStorage!');
    }

    return headers;
  }

  // Helper method for error handling with detailed logging
  private handleError = (operation = 'operation') => {
    return (error: HttpErrorResponse): Observable<never> => {
      console.error(`❌ ${operation} failed:`, error);
      console.error('Status:', error.status);
      console.error('Status Text:', error.statusText);
      console.error('Error Body:', error.error);
      console.error('Full URL:', error.url);

      if (error.status === 401) {
        console.error('🚨 UNAUTHORIZED - Check your token!');
        // Optionally redirect to login
        // this.router.navigate(['/login']);
      }

      return throwError(() => error);
    };
  };

  addAccount(data: any): Observable<any> {
  const url = `${this.baseUrl}/clients/add-account`;
  return this.http.post(url, data, {
    headers: this.getAuthHeaders(),  // ⬅️ Ce header doit inclure le token !
    responseType: 'json'
  });
}




  getAccountCount(): Observable<number> {
    console.log('📊 Calling getAccountCount()');
    const url = `${this.baseUrl}/accounts/count`;
    console.log('🌐 URL:', url);

    return this.http.get<number>(url, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Account count result:', result)),
      catchError(this.handleError('getAccountCount'))
    );
  }

  getClientCount(): Observable<number> {
    console.log('👥 Calling getClientCount()');
    const url = `${this.baseUrl}/clients/count`;
    console.log('🌐 URL:', url);

    return this.http.get<number>(url, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Client count result:', result)),
      catchError(this.handleError('getClientCount'))
    );
  }

  getBasicClients(): Observable<ClientBasicDTO[]> {
    console.log('📋 Calling getBasicClients()');
    const url = `${this.baseUrl}/clients/basic`;
    console.log('🌐 URL:', url);

    return this.http.get<ClientBasicDTO[]>(url, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Basic clients result:', result?.length, 'items')),
      catchError(this.handleError('getBasicClients'))
    );
  }

  getAllClients(): Observable<ClientSummaryDTO[]> {
    console.log('📋 Calling getAllClients()');
    const url = `${this.baseUrl}/clients/detailed`;
    console.log('🌐 URL:', url);

    return this.http.get<ClientSummaryDTO[]>(url, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => {
        console.log('✅ All clients result:', result?.length, 'items');
        console.log('First client sample:', result?.[0]);
      }),
      catchError(this.handleError('getAllClients'))
    );
  }

  getClientById(id: string): Observable<ClientSummaryDTO> {
    console.log('🔍 Calling getClientById() with ID:', id);
    const url = `${this.baseUrl}/clients/${id}`;
    console.log('🌐 URL:', url);

    return this.http.get<ClientSummaryDTO>(url, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Client by ID result:', result)),
      catchError(this.handleError('getClientById'))
    );
  }

  searchClientsByName(name: string): Observable<ClientSummaryDTO[]> {
    console.log('🔍 Calling searchClientsByName() with name:', name);
    const url = `${this.baseUrl}/clients/search?name=${encodeURIComponent(name)}`;
    console.log('🌐 URL:', url);

    return this.http.get<ClientSummaryDTO[]>(url, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Search clients result:', result?.length, 'items')),
      catchError(this.handleError('searchClientsByName'))
    );
  }

  createClient(data: any): Observable<any> {
    console.log('➕ Calling createClient() with data:', data);
    const url = `${this.baseUrl}/enroll`;
    console.log('🌐 URL:', url);

    return this.http.post(url, data, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Create client result:', result)),
      catchError(this.handleError('createClient'))
    );
  }

  updateClient(data: ClientUpdateRequest): Observable<any> {
  console.log('✏️ Calling updateClient() with data:', data);
  const url = `${this.baseUrl}/update`;
  console.log('🌐 URL:', url);

  return this.http.put(url, data, {
    headers: this.getAuthHeaders(),
    responseType: 'text' // << ✅ ajouter ça
  }).pipe(
    tap(result => console.log('✅ Update client result:', result)),
    catchError(this.handleError('updateClient'))
  );
}


  deleteClient(payload: { clientId: string; supervisorCode: string }): Observable<any> {
    console.log('🗑️ Calling deleteClient() with payload:', payload);
    const url = `${this.baseUrl}/delete`;
    console.log('🌐 URL:', url);

    return this.http.request('delete', url, {
      body: payload,
      headers: this.getAuthHeaders()
    }).pipe(
      tap(result => console.log('✅ Delete client result:', result)),
      catchError(this.handleError('deleteClient'))
    );
  }

  toggleClientStatus(clientId: string, compteBloque: boolean, documentsComplets: boolean): Observable<any> {
    console.log('🔄 Calling toggleClientStatus() for client:', clientId);
    const url = `${this.baseUrl}/clients/${clientId}/status`;
    console.log('🌐 URL:', url);
    console.log('📊 Params:', { compteBloque, documentsComplets });

    return this.http.put(url, null, {
  headers: this.getAuthHeaders(),
  params: {
    compteBloque: compteBloque.toString(),
    documentsComplets: documentsComplets.toString()
  },
  responseType: 'text' as 'json' // 👈 ajoute cette ligne
});

  }
}
