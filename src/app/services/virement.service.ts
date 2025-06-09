import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class VirementService {
  private apiUrl = 'http://localhost:8080/employee/transfer'; // adapte l'URL si besoin

  constructor(private http: HttpClient) {}

  effectuerVirement(data: { fromAccountId: number; toRib: string; amount: number }): Observable<string> {
    const token = localStorage.getItem('token') || '';
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    });

    return this.http.post(this.apiUrl, data, { headers, responseType: 'text' });
  }
}
