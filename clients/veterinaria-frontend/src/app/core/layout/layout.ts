import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../auth/auth-service';

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="barra">
      <a routerLink="/" class="marca">🐾 Veterinaria</a>
      <nav>
        <a routerLink="/especies" routerLinkActive="activo">Especies</a>
        @if (auth.tieneRol('CLIENTE')) {
          <a routerLink="/citas/nueva" routerLinkActive="activo">Nueva cita</a>
          <a routerLink="/mis-citas" routerLinkActive="activo">Mis citas</a>
        }
        @if (auth.tieneRol('ADMIN')) {
          <a routerLink="/pagos" routerLinkActive="activo">Pagos</a>
        }
      </nav>
      <div class="sesion-area">
        @if (auth.autenticado()) {
          <span class="usuario">{{ auth.nombre() }} · {{ auth.roles().join(', ') }}</span>
          <button (click)="auth.cerrarSesion()">Cerrar sesión</button>
        } @else {
          <button (click)="auth.iniciarLogin('/')">Iniciar sesión</button>
        }
      </div>
    </header>
    <main class="contenido">
      <router-outlet />
    </main>
  `,
  styles: `
    .barra { display: flex; align-items: center; gap: 1.5rem; padding: .75rem 1.5rem; background: #0f766e; color: #fff; }
    .marca { font-weight: 700; color: #fff; text-decoration: none; font-size: 1.1rem; }
    nav { display: flex; gap: 1rem; }
    nav a { color: #ccfbf1; text-decoration: none; }
    nav a.activo { color: #fff; font-weight: 600; text-decoration: underline; }
    .sesion-area { margin-left: auto; display: flex; align-items: center; gap: .75rem; }
    .usuario { font-size: .9rem; }
    .contenido { max-width: 1000px; margin: 1.5rem auto; padding: 0 1rem; }
  `,
})
export class Layout {
  protected readonly auth = inject(AuthService);
}
