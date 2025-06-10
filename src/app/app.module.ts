import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

// Angular Material Modules
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';

// App Components
import { AppComponent } from './app.component';
import { SetPasswordComponent } from './components/set-password/set-password.component';
import { ChatbotComponent } from './components/chatbot/chatbot.component';
import { ClientDashboardComponent } from './client-dashboard/client-dashboard.component';

// Services
import { AuthService } from './services/auth.service';
import { RechargeService } from './services/recharge.service';
import { CryptoService } from './services/crypto.service';
import { AuthInterceptor } from './services/auth.interceptor';
// import { AccountsService } from './services/accounts.service';

// Routing
import { routes } from './app.routes';

// Toastr
import { ToastrModule } from 'ngx-toastr';

@NgModule({
  declarations: [

  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    RouterModule.forRoot(routes),
    FormsModule,
    BrowserAnimationsModule,
    ToastrModule.forRoot({
      positionClass: 'toast-bottom-right',
      preventDuplicates: true
    }),
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatSelectModule,
    AppComponent,
    SetPasswordComponent,
    ChatbotComponent,
    ClientDashboardComponent
  ],
  providers: [
    AuthService,
    RechargeService,
    CryptoService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}