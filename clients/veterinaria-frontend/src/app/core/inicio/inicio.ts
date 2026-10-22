import { Component, inject } from '@angular/core';
import { AuthService } from '../auth/auth-service';

@Component({
  selector: 'app-inicio',
  template: `
    <h1>Clínica veterinaria</h1>
    <p>
      Cliente Angular 22 que consume los microservicios a través del Gateway (puerto 18080) y se
      autentica con Keycloak (Authorization Code + PKCE).
    </p>
    @if (auth.autenticado()) {
      <table class="tabla">
        <tr><th>Usuario</th><td>{{ auth.usuario() }}</td></tr>
        <tr><th>Roles (realm_access.roles)</th><td>{{ auth.roles().join(', ') }}</td></tr>
        <tr><th>sub (cuenta Keycloak)</th><td><code>{{ auth.sub() }}</code></td></tr>
        <tr><th>idCliente (protocol mapper)</th><td>{{ auth.idCliente() ?? 'sin perfil de cliente' }}</td></tr>
      </table>
    } @else {
      <p>Página pública. Inicia sesión para ver especies, agendar citas o revisar pagos.</p>
    }
  `,
})
export class Inicio {
  protected readonly auth = inject(AuthService);
}
