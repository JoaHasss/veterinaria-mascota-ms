import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth-service';

/**
 * Oculta pantallas a quien no tiene el rol. Es solo experiencia de usuario:
 * el limite real de seguridad es el Gateway, que responde 403 igual.
 * Va despues de authGuard en canActivate.
 */
export const rolGuard =
  (...roles: string[]): CanActivateFn =>
  () => {
    const auth = inject(AuthService);
    if (auth.tieneRol(...roles)) {
      return true;
    }
    alert(`Esta pantalla requiere el rol ${roles.join(' o ')}.`);
    return inject(Router).parseUrl('/');
  };
