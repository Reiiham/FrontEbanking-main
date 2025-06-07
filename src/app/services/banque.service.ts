import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
<<<<<<< HEAD
import { ClientBasicDTO } from '../../../src/app/b';
import { ClientSummaryDTO } from '../../banque/models/client-summary.model';
=======
import { ClientBasicDTO } from '../banque/models/client-basic.model';
import { ClientSummaryDTO } from '../banque/models/client-summary.model';
>>>>>>> master
import { ClientUpdateRequest } from '../banque/models/client-update-request.model';
import { environment } from '../environments/environment';

@Injectable({ providedIn: 'root' })
export class ClientService {
  private baseUrl = `${environment.apiUrl}/employee`;

  constructor(private http: HttpClient) {}


  getAccountCount(): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/accounts/count`);
  }
   
  getClientCount(): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/clients/count`);
  }



  getBasicClients(): Observable<ClientBasicDTO[]> {
    return this.http.get<ClientBasicDTO[]>(`${this.baseUrl}/clients/basic`);
  }
  getAllClients(): Observable<ClientSummaryDTO[]> {
  // const token = localStorage.getItem('token');
  // console.log('Appel getAllClients(), token =', token);
  return this.http.get<ClientSummaryDTO[]>(`${this.baseUrl}/clients/detailed`);
}

  getClientById(id: string): Observable<ClientSummaryDTO> {
  return this.http.get<ClientSummaryDTO>(`${this.baseUrl}/clients/${id}`);
}


 searchClientsByName(name: string): Observable<ClientSummaryDTO[]> {
  return this.http.get<ClientSummaryDTO[]>(
    `${this.baseUrl}/clients/search?name=${encodeURIComponent(name)}`
  );
}




  createClient(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/enroll`, data);
  }

  updateClient(data: ClientUpdateRequest): Observable<any> {
    return this.http.put(`${this.baseUrl}/update`, data);
  }

  deleteClient(payload: { clientId: string; supervisorCode: string }): Observable<any> {
  return this.http.request('delete', `${this.baseUrl}/delete`, {
    body: payload
  });
}


  toggleClientStatus(clientId: string, compteBloque: boolean, documentsComplets: boolean): Observable<any> {
  return this.http.put(
    `${this.baseUrl}/clients/${clientId}/status`,
    null, // PUT sans body, car params sont dans l'URL
    {
      params: {
        compteBloque: compteBloque.toString(),
        documentsComplets: documentsComplets.toString()
      }
    }
  );
}

}