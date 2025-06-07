import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../environments/environment';

export interface EmployeeDTO {
  firstName: string;
  lastName: string;
  phoneNumber: string;
  cin:  string;
  emailPersonnel: string;

}

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  private apiUrl = `${environment.apiUrl}/admin/employees`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('No token found');
      throw new Error('No token found');
    }
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  createEmployee(employee: EmployeeDTO): Observable<any> {
    return this.http.post(this.apiUrl, employee, { headers: this.getHeaders() }).pipe(
      catchError(err => {
        console.error('Error creating employee:', err);
        const errorMessage = err.error?.error || err.error?.message || 'Failed to create employee';
        return throwError(() => new Error(errorMessage));
      })
    );
  }
}
