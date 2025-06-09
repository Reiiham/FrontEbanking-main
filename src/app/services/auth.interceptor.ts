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
        console.warn('[Interceptor] 🚨 401 detected!');
        this.toastr.error('Votre session a expiré.', 'Authentification requise');
        this.authService.logout();
        this.router.navigate(['/login']);
      }


        return throwError(() => error);
      })
    );
  }
}
