import { Injectable } from '@angular/core';
<<<<<<< HEAD
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
=======
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
>>>>>>> master
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
<<<<<<< HEAD
import { Location } from '@angular/common';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService, private router: Router, private location: Location) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('token');
    let authReq = req;

=======
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
    console.log('Intercepting request', req.url, 'token =', token); // 🔍 TEST

    let authReq = req;
>>>>>>> master
    if (token) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
<<<<<<< HEAD
        if ((error.status === 401 || error.status === 403) && this.location.path() !== '/login') {
          this.authService.logout();
          this.router.navigate(['/login']);
        }
        return throwError(() => new Error(error.message || 'Server error'));
=======
        if (error.status === 401 || error.status === 403) {
          this.authService.logout();
          this.toastr.error('Session expirée. Veuillez vous reconnecter.');
          this.router.navigate(['/login']);
        }
        return throwError(() => new Error(error.message));
>>>>>>> master
      })
    );
  }
}
