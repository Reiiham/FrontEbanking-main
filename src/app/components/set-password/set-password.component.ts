import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-set-password',
  templateUrl: './set-password.component.html',
  styleUrls: ['./set-password.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ]
})
export class SetPasswordComponent implements OnInit {
  setPasswordForm: FormGroup;
  token: string = '';
  isLoading = false;
  tokenValid = false;
  tokenChecked = false;
  errorMessage = '';
  successMessage = '';
  showPassword = false;
  showConfirmPassword = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    public router: Router, // Ajout de 'public' pour l'utiliser dans le template
    private authService: AuthService
  ) {
    this.setPasswordForm = this.fb.group({
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/)
      ]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    // Récupérer le token depuis l'URL
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (this.token) {
        this.validateToken();
      } else {
        this.errorMessage = 'Token manquant dans l\'URL';
        this.tokenChecked = true;
      }
    });
  }

  validateToken(): void {
    this.isLoading = true;
    this.authService.validateToken(this.token).subscribe({
      next: (response) => {
        this.tokenValid = response.valid;
        this.tokenChecked = true;
        this.isLoading = false;
        if (!this.tokenValid) {
          this.errorMessage = response.message || 'Token invalide ou expiré';
        }
      },
      error: (error) => {
        this.tokenValid = false;
        this.tokenChecked = true;
        this.isLoading = false;
        this.errorMessage = 'Erreur lors de la validation du token';
        console.error('Token validation error:', error);
      }
    });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');

    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }

    if (confirmPassword?.hasError('passwordMismatch')) {
      delete confirmPassword.errors!['passwordMismatch'];
      if (Object.keys(confirmPassword.errors!).length === 0) {
        confirmPassword.setErrors(null);
      }
    }

    return null;
  }

  onSubmit(): void {
    if (this.setPasswordForm.valid && this.tokenValid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const request = {
        token: this.token,
        newPassword: this.setPasswordForm.get('password')?.value
      };

      this.authService.setPassword(request).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.successMessage = 'Mot de passe défini avec succès ! Redirection vers la page de connexion...';

          // Rediriger vers la page de connexion après 3 secondes
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 3000);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.error || 'Erreur lors de la définition du mot de passe';
          console.error('Set password error:', error);
        }
      });
    }
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  getPasswordErrors(): string[] {
    const errors: string[] = [];
    const passwordControl = this.setPasswordForm.get('password');

    if (passwordControl?.hasError('required')) {
      errors.push('Le mot de passe est requis');
    }
    if (passwordControl?.hasError('minlength')) {
      errors.push('Le mot de passe doit contenir au moins 8 caractères');
    }
    if (passwordControl?.hasError('pattern')) {
      errors.push('Le mot de passe doit contenir au moins : une majuscule, une minuscule, un chiffre et un caractère spécial');
    }

    return errors;
  }
  isMinLength(password: string): boolean {
    return password?.length >= 8;
  }

  hasUpperCase(password: string): boolean {
    return /[A-Z]/.test(password || '');
  }

  hasLowerCase(password: string): boolean {
    return /[a-z]/.test(password || '');
  }

  hasNumber(password: string): boolean {
    return /\d/.test(password || '');
  }

  hasSpecialChar(password: string): boolean {
    return /[@$!%*?&]/.test(password || '');
  }


  resendActivationLink(): void {
    // Cette fonction pourrait être implémentée pour renvoyer un nouveau lien
    // si vous avez l'email de l'utilisateur
    console.log('Fonctionnalité de renvoi de lien à implémenter');
  }
}
