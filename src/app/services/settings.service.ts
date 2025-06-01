import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AppSetting } from '../admin/models/app-setting.model';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SettingsService {
  private apiUrl = `${environment.apiUrl}/admin/settings`;

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

  getSettings(): Observable<AppSetting[]> {
    return this.http.get<AppSetting[]>(this.apiUrl, { headers: this.getHeaders() }).pipe(
      catchError(err => {
        console.error('Error fetching settings:', err);
        return throwError(() => new Error('Failed to fetch settings: ' + (err.error?.message || err.message)));
      })
    );
  }

  addSetting(setting: AppSetting): Observable<AppSetting> {
    return this.http.post<AppSetting>(this.apiUrl, setting, { headers: this.getHeaders() }).pipe(
      catchError(err => {
        console.error('Error adding setting:', err);
        return throwError(() => new Error('Failed to add setting: ' + (err.error?.message || err.message)));
      })
    );
  }

  updateSetting(id: number, setting: AppSetting): Observable<AppSetting> {
    console.log('Sending update request for setting:', setting);
    return this.http.put<AppSetting>(`${this.apiUrl}/${id}`, setting, { headers: this.getHeaders() }).pipe(
      catchError(err => {
        console.error('Error updating setting:', err);
        return throwError(() => new Error('Failed to update setting: ' + (err.error?.message || err.message)));
      })
    );
  }

  updateSettingByKey(key: string, value: string): Observable<AppSetting> {
    return this.http.put<AppSetting>(`${this.apiUrl}/by-key/${key}`, { value }, { headers: this.getHeaders() }).pipe(
      catchError(err => {
        console.error('Error updating setting by key:', err);
        return throwError(() => new Error('Failed to update setting by key: ' + (err.error?.message || err.message)));
      })
    );
  }

  deleteSetting(id: number): Observable<void> {
    const url = `${this.apiUrl}/${id}`;
    console.log('Sending DELETE request to:', url);
    return this.http.delete<void>(url, { headers: this.getHeaders() }).pipe(
      catchError(err => {
        console.error('Error deleting setting:', err);
        return throwError(() => new Error('Failed to delete setting: ' + (err.error?.message || err.message)));
      })
    );
  }
}
