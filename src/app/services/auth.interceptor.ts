import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { ToastrService } from 'ngx-toastr';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    console.log('[Interceptor] Sending to:', req.url);
    console.log('[Interceptor] Token:', token);

    let authReq = req;
    if (token) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        console.error('[Interceptor] Error:', error);

        if (error.status === 401) {
          console.warn('[Interceptor] 🚨 401 detected on:', req.url);
          
          // ✅ Ne pas rediriger automatiquement pour certaines opérations
          const shouldSkipRedirect = this.shouldSkipAutoRedirect(req.url, error);
          
          if (!shouldSkipRedirect) {
            console.warn('[Interceptor] Auto-redirecting to login');
            this.toastr.error('Votre session a expiré.', 'Authentification requise');
            this.authService.logout();
            this.router.navigate(['/login']);
          } else {
            console.warn('[Interceptor] Skipping auto-redirect for:', req.url);
          }
        }

        return throwError(() => error);
      })
    );
  }

  /**
   * Détermine si on doit éviter la redirection automatique
   */
  private shouldSkipAutoRedirect(url: string, error: HttpErrorResponse): boolean {
    // Skip pour les opérations d'update/delete qui peuvent avoir des erreurs business
    if (url.includes('/update') || url.includes('/delete')) {
      return true;
    }
    
    // Skip si l'erreur contient un message spécifique (ex: "Invalid supervisor code")
    if (error.error && typeof error.error === 'string') {
      const errorMsg = error.error.toLowerCase();
      if (errorMsg.includes('supervisor') || errorMsg.includes('password') || errorMsg.includes('code')) {
        return true;
      }
    }
    
    return false;
  }
}