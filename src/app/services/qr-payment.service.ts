import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

interface QrPaymentRequest {
  rib: string;
  amount: number;
  description: string;
}

interface ApiResponse {
  success: boolean;
  data: string; // image en base64
  error: string | null;
}

@Injectable({ providedIn: 'root' })
export class QrPaymentService {
  private apiUrl = 'http://localhost:8090/eBankingVer1_war_exploded/api/v1/qr-payments';

  constructor(private http: HttpClient) {}

  generateQr(request: QrPaymentRequest): Observable<ApiResponse> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`
    });
    return this.http.post<ApiResponse>(`${this.apiUrl}/generate`, request, { headers });
  }
}
