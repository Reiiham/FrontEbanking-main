import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse, HttpResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import {catchError, tap} from 'rxjs/operators';
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
    console.log('🔍 Intercepting request:', req.url, 'token exists:', !!token);

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
        console.log('❌ HTTP Error caught by interceptor:');
        console.log('   URL:', error.url);
        console.log('   Status:', error.status);
        console.log('   Message:', error.message);
        console.log('   Full error:', error);

        if (error.status === 401 || error.status === 403) {
          console.log('🚨 AUTH ERROR DETECTED - About to logout and redirect');
          console.log('   This is likely the cause of your logout issue!');

          // 🔧 TEMPORARY: Comment out the logout for debugging
          // this.authService.logout();
          // this.toastr.error('Session expirée. Veuillez vous reconnecter.');
          // this.router.navigate(['/login']);

          // 🔧 TEMPORARY: Just log instead of redirecting
          console.log('🔧 LOGOUT DISABLED FOR DEBUGGING - Would normally redirect to login');
          this.toastr.warning('Auth error detected but logout disabled for debugging');
        }

        return throwError(() => new Error(error.message || 'Erreur serveur'));
      })
    );
  }
}
