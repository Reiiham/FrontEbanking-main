import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from './services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    console.log('🛡️ AuthGuard triggered for route:', state.url);

    const isLoggedIn = this.authService.isLoggedIn();
    console.log('🛡️ AuthGuard check, isLoggedIn:', isLoggedIn);

    if (isLoggedIn) {
      console.log('✅ AuthGuard: Access granted');
      return true;
    }

    console.warn('❌ AuthGuard: Redirecting to /login – user not authenticated');
    console.warn('❌ Current route was:', state.url);
    this.router.navigate(['/login']);
    return false;
  }
}
