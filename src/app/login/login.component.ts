import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { FormsModule } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  standalone: true
})
export class LoginComponent implements OnInit {
  username: string = '';
  password: string = '';
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    console.log('LoginComponent initialized');
  }

  onSubmit(): void {
    this.errorMessage = '';
    if (!this.username || !this.password) {
      this.errorMessage = 'Please fill in both username and password.';
      return;
    }

    this.isLoading = true;
    console.log('Attempting login with username:', this.username);
    this.authService.login(this.username, this.password).subscribe({
      next: (response) => {
        console.log('Login response:', response);
        if (response.role === 'CLIENT') {
          let clientId = response.clientId || this.authService.getClientId();
          if (clientId) {
            this.isLoading = false;
            console.log('Navigating to client dashboard with clientId:', clientId);
            this.router.navigate([`/client/${clientId}/dashboard`]);
          } else {
            console.log('No clientId in response, fetching profile');
            this.authService.getClientProfile().subscribe({
              next: (profile) => {
                this.isLoading = false;
                clientId = profile.clientId;
                console.log('Profile fetched:', profile);
                if (clientId) {
                  this.router.navigate([`/client/${clientId}/dashboard`]);
                } else {
                  this.isLoading = false;
                  this.errorMessage = 'Client ID not found in profile';
                  console.error('No clientId in profile:', profile);
                }
              },
              error: (err) => {
                this.isLoading = false;
                this.errorMessage = err.message || 'Failed to fetch client profile';
                console.error('Profile fetch error:', err);
              }
            });
          }
        } else if (response.role === 'ADMIN') {
          this.isLoading = false;
          this.router.navigate(['/admin/dashboard']);
        } else if (response.role === 'EMPLOYEE') {
          this.isLoading = false;
          this.router.navigate(['/employee/dashboard']);
        } else {
          this.isLoading = false;
          this.errorMessage = 'Unrecognized role: ' + response.role;
          console.error('Unrecognized role:', response.role);
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.message || 'Invalid credentials';
        console.error('Login error:', err);
      }
    });
  }
}
