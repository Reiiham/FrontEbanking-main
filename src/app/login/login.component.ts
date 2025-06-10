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
import { LoginResponse } from '../model/login-response.model'; // Adjust the import path as necessary

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
  pin: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;
  requires2FA: boolean = false;
  currentUsername: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    console.log('LoginComponent initialized');
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.username || !this.password) {
      this.errorMessage = 'Veuillez remplir le nom d\'utilisateur et le mot de passe.';
      return;
    }

    this.isLoading = true;
    console.log('Attempting login with username:', this.username);

    this.authService.login(this.username, this.password).subscribe({
      next: (response) => {
        console.log('Login response:', response);

        // Vérifier si 2FA est requis
        if (response.requires2FA === 'true' || response.requires2FA === true) {
          this.isLoading = false;
          this.requires2FA = true;
          this.currentUsername = response.username || this.username;
          this.successMessage = response.message || 'Code de vérification envoyé à votre téléphone.';
          // Effacer le mot de passe pour la sécurité
          this.password = '';
        } else {
          // Login direct pour les non-CLIENT
          this.handleSuccessfulLogin(response);
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.message || 'Identifiants invalides';
        console.error('Login error:', err);
      }
    });
  }

  onVerify2FA(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.pin) {
      this.errorMessage = 'Veuillez saisir le code PIN.';
      return;
    }

    this.isLoading = true;
    console.log('Attempting 2FA verification for username:', this.currentUsername);

    this.authService.verify2FA(this.currentUsername, this.pin).subscribe({
      next: (response) => {
        console.log('2FA verification response:', response);
        this.handleSuccessfulLogin(response);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.message || 'Code PIN invalide ou expiré';
        console.error('2FA verification error:', err);
        // Effacer le PIN en cas d'erreur
        this.pin = '';
      }
    });
  }

  onResend2FA(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.isLoading = true;

    this.authService.resend2FA(this.currentUsername).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = response.message || 'Nouveau code envoyé.';
        console.log('2FA resend response:', response);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.message || 'Erreur lors de l\'envoi du code';
        console.error('2FA resend error:', err);
      }
    });
  }

  onBackToLogin(): void {
    this.requires2FA = false;
    this.currentUsername = '';
    this.pin = '';
    this.errorMessage = '';
    this.successMessage = '';
    this.password = '';
  }

  private handleSuccessfulLogin(response: any): void {
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
              this.errorMessage = 'Client ID introuvable dans le profil';
              console.error('No clientId in profile:', profile);
            }
          },
          error: (err) => {
            this.isLoading = false;
            this.errorMessage = err.message || 'Échec de la récupération du profil client';
            console.error('Profile fetch error:', err);
          }
        });
      }
    } else if (response.role === 'ADMIN') {
      this.isLoading = false;
      this.router.navigate(['/admin/dashboard']);
    } else if (response.role === 'EMPLOYEE') {
      this.isLoading = false;

      this.router.navigate(['/employee']);
    } else {
      this.isLoading = false;
      this.errorMessage = 'Rôle non reconnu: ' + response.role;
      console.error('Unrecognized role:', response.role);
    }
  }
}
