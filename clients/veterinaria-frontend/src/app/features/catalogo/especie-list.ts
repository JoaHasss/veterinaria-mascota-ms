import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth-service';
import { mensajeError } from '../../core/http-error';
import { CatalogoService, Especie } from './catalogo-service';

@Component({
  selector: 'app-especie-list',
  imports: [RouterLink],
  template: `
    <div class="cabecera">
      <h2>Especies</h2>
      @if (auth.tieneRol('ADMIN')) {
        <a routerLink="/especies/nueva" class="boton">Nueva especie</a>
      }
    </div>
    @if (errorAccion()) {
      <p class="error">{{ errorAccion() }}</p>
    }
    @if (cargando()) {
      <p>Cargando...</p>
    } @else if (error()) {
      <p class="error">{{ error() }}</p>
    } @else {
      <table class="tabla">
        <thead><tr><th>ID</th><th>Nombre</th><th>Descripción</th><th></th></tr></thead>
        <tbody>
          @for (e of especies(); track e.id) {
            <tr>
              <td>{{ e.id }}</td>
              <td>{{ e.nombre }}</td>
              <td>{{ e.descripcion }}</td>
              <td class="acciones">
                @if (auth.tieneRol('ADMIN')) {
                  <a [routerLink]="['/especies', e.id, 'editar']">Editar</a>
                  <button class="peligro" (click)="eliminar(e)">Eliminar</button>
                }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="4">No hay especies registradas.</td></tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class EspecieList implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly catalogo = inject(CatalogoService);

  protected readonly especies = signal<Especie[]>([]);
  protected readonly cargando = signal(true);
  /** La lista no cargo. */
  protected readonly error = signal<string | null>(null);
  /** Una accion puntual fallo; la lista sigue visible. */
  protected readonly errorAccion = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  protected eliminar(especie: Especie): void {
    if (!confirm(`Eliminar la especie ${especie.nombre}?`)) return;
    this.errorAccion.set(null);
    this.catalogo.eliminarEspecie(especie.id!).subscribe({
      next: () => this.cargar(),
      error: (err) => this.errorAccion.set(
        err.status === 500
          ? `No se pudo eliminar ${especie.nombre}: tiene mascotas asociadas.`
          : mensajeError(err, 'eliminar la especie'),
      ),
    });
  }

  private cargar(): void {
    this.cargando.set(true);
    this.catalogo.listarEspecies().subscribe({
      next: (lista) => {
        this.especies.set(lista);
        this.error.set(null);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeError(err, 'cargar las especies'));
        this.cargando.set(false);
      },
    });
  }
}
