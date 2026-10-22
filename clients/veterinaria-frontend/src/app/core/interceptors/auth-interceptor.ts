import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth-service';

/**
 * Agrega Authorization: Bearer solo a las llamadas al Gateway. Un 401 indica
 * token ausente o vencido: se reinicia el login. El 403 (token valido pero
 * sin rol) no se maneja aqui: lo muestra cada pantalla.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  if (!req.url.startsWith(environment.apiBaseUrl)) {
    return next(req);
  }
  const token = auth.token();
  const peticion = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(peticion).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        auth.iniciarLogin(window.location.pathname);
      }
      return throwError(() => err);
    }),
  );
};
