import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth-interceptor';
import { traceInterceptor } from './core/interceptors/trace-interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    // Sin withInterceptors ninguna peticion llevaria el token ni el X-Trace-ID.
    provideHttpClient(withInterceptors([authInterceptor, traceInterceptor])),
  ]
};
