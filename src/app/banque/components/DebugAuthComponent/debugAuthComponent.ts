// Debug Component - Add this to test your auth
import {Component} from '@angular/core';
import {ClientService} from '../../../services/banque.service';
import {JsonPipe} from '@angular/common';

@Component({
  selector: 'app-debug-auth',
  standalone: true,
  imports: [
    JsonPipe
  ],
  template: `
    <div style="padding: 20px; border: 2px solid #ff6b6b; margin: 10px;">
      <h3>🔧 Auth Debug Panel</h3>
      <button (click)="checkAuth()" style="margin: 5px; padding: 10px;">Check Auth Status</button>
      <button (click)="testClientCount()" style="margin: 5px; padding: 10px;">Test Client Count</button>
      <button (click)="testGetClients()" style="margin: 5px; padding: 10px;">Test Get Clients</button>
      <div *ngIf="debugInfo" style="margin-top: 10px; background: #f5f5f5; padding: 10px;">
        <pre>{{ debugInfo | json }}</pre>
      </div>
    </div>
  `
})
export class DebugAuthComponent {
  debugInfo: any = null;

  constructor(private clientService: ClientService) {}

  checkAuth() {
    const tokens = {
      token: localStorage.getItem('token'),
      authToken: localStorage.getItem('authToken'),
      jwt: localStorage.getItem('jwt'),
      allKeys: Object.keys(localStorage)
    };

    this.debugInfo = {
      message: 'Auth Check',
      tokens,
      timestamp: new Date().toISOString()
    };

    console.log('🔍 Auth Debug:', this.debugInfo);
  }

  testClientCount() {
    this.clientService.getClientCount().subscribe({
      next: (count) => {
        this.debugInfo = {
          message: 'Client Count Success',
          count,
          timestamp: new Date().toISOString()
        };
      },
      error: (error) => {
        this.debugInfo = {
          message: 'Client Count Failed',
          error: error.message,
          status: error.status,
          timestamp: new Date().toISOString()
        };
      }
    });
  }

  testGetClients() {
    this.clientService.getAllClients().subscribe({
      next: (clients) => {
        this.debugInfo = {
          message: 'Get Clients Success',
          clientCount: clients?.length || 0,
          firstClient: clients?.[0] || null,
          timestamp: new Date().toISOString()
        };
      },
      error: (error) => {
        this.debugInfo = {
          message: 'Get Clients Failed',
          error: error.message,
          status: error.status,
          timestamp: new Date().toISOString()
        };
      }
    });
  }
}
