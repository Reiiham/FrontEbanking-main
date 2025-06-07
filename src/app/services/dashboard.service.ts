import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = 'http://localhost:8090/eBankingVer1_war_exploded/api/admin'; // Adjusted to match admin endpoint

  constructor(private http: HttpClient, private authService: AuthService) {}

  getStats(): Observable<{ totalCurrencies: number; totalSettings: number; lastUpdate: string }> {
    const token = this.authService.isLoggedIn() ? localStorage.getItem('token') : null;
    if (!token) {
      console.error('No token available for stats request');
      throw new Error('Authentication required');
    }

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.get<{ totalCurrencies: number; totalSettings: number; lastUpdate: string }>(
      `${this.apiUrl}/dashboard`, // Matches /api/admin/dashboard
      { headers }
    );
  }
}
