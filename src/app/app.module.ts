<<<<<<< HEAD
// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';
=======
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

// Angular Material Modules
>>>>>>> master
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';

<<<<<<< HEAD
import { AppComponent } from './app.component';
import { AuthService } from './services/auth.service';
import { AccountsService } from './services/accounts.service';
import { AuthInterceptor } from './services/auth.interceptor';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { routes } from './app.routes';
import {ChatbotComponent} from './components/chatbot/chatbot.component';
import {FormsModule} from '@angular/forms';
import { SetPasswordComponent } from './components/set-password/set-password.component';

@NgModule({
  declarations: [],
=======
// App Components & Routing
import { AppComponent } from './app.component';
import { SetPasswordComponent } from './components/set-password/set-password.component';
import { ChatbotComponent } from './components/chatbot/chatbot.component';
import { routes } from './app.routes';

// Services
import { AuthService } from './services/auth.service';
import { AccountsService } from './services/accounts.service';
import { AuthInterceptor } from './services/auth.interceptor';

// Toastr (si utilisé)
import { ToastrModule } from 'ngx-toastr';

@NgModule({
  declarations: [
    AppComponent,
    SetPasswordComponent,
    ChatbotComponent
  ],
>>>>>>> master
  imports: [
    BrowserModule,
    HttpClientModule,
    RouterModule.forRoot(routes),
<<<<<<< HEAD
=======
    FormsModule,
    BrowserAnimationsModule,
    ToastrModule.forRoot({
      positionClass: 'toast-bottom-right',
      preventDuplicates: true
    }),

    // Angular Material
>>>>>>> master
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatTableModule,
<<<<<<< HEAD
    MatSelectModule,
    AppComponent,
    FormsModule,
    SetPasswordComponent
=======
    MatSelectModule
>>>>>>> master
  ],
  providers: [
    AuthService,
    AccountsService,
<<<<<<< HEAD
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
=======
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
>>>>>>> master
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
