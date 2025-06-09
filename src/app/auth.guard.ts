import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    const token = this.authService.getToken();
    console.log('[AuthGuard] Token exists:', !!token);

    if (!token) {
      console.warn('[AuthGuard] ❌ No token found → redirecting to /login');
      this.router.navigate(['/login']);
      return false;
    }

    // Vérification du format et de l'expiration du token JWT
    try {
      if (token.includes('.')) {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const isExpired = payload.exp && payload.exp < Date.now() / 1000;

        if (isExpired) {
          console.warn('[AuthGuard] ⚠️ Token expired at', new Date(payload.exp * 1000));
          this.authService.logout(); // vide le localStorage proprement
          this.router.navigate(['/login']);
          return false;
        }

        console.log('[AuthGuard] ✅ Token is valid until', new Date(payload.exp * 1000));
      } else {
        console.warn('[AuthGuard] ⚠️ Token format is not JWT (no .)');
      }

      return true;
    } catch (error) {
      console.error('[AuthGuard] ❌ Invalid token format or parsing failed', error);
      this.authService.logout(); // en cas d'erreur → sécurité
      this.router.navigate(['/login']);
      return false;
    }
  }
}
