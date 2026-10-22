import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth-service';

/** Ruta /auth/callback: Keycloak vuelve aqui con ?code=...&state=... */
@Component({
  selector: 'app-auth-callback',
  template: `
    @if (error()) {
      <p class="error">{{ error() }}</p>
      <button (click)="reintentar()">Iniciar sesion de nuevo</button>
    } @else {
      <p>Validando sesion con Keycloak...</p>
    }
  `,
})
export class AuthCallback implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly error = signal<string | null>(null);

  async ngOnInit(): Promise<void> {
    try {
      const destino = await this.auth.completarLogin(new URLSearchParams(window.location.search));
      await this.router.navigateByUrl(destino, { replaceUrl: true });
    } catch (e) {
      this.error.set(e instanceof Error ? e.message : String(e));
    }
  }

  protected reintentar(): void {
    this.auth.iniciarLogin('/');
  }
}
