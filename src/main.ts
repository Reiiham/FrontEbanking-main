import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideToastr } from 'ngx-toastr';

import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { importProvidersFrom } from '@angular/core';
import { ToastrModule } from 'ngx-toastr';


bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withFetch()),

    provideAnimations(), // 👈 Required for Toastr
    provideToastr({
      positionClass: 'toast-bottom-right',
      preventDuplicates: true
    })

  ]
});

