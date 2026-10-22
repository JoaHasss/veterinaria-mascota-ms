import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { mensajeError } from '../../core/http-error';
import { CatalogoService } from './catalogo-service';

@Component({
  selector: 'app-especie-form',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <h2>{{ id() ? 'Editar especie' : 'Nueva especie' }}</h2>
    <form [formGroup]="form" (ngSubmit)="guardar()" class="formulario">
      <label>Nombre <input formControlName="nombre" maxlength="100" /></label>
      <label>Descripción <input formControlName="descripcion" maxlength="255" /></label>
      @if (error()) {
        <p class="error">{{ error() }}</p>
      }
      <div class="acciones">
        <button type="submit" [disabled]="form.invalid || guardando()">Guardar</button>
        <a routerLink="/especies">Cancelar</a>
      </div>
    </form>
  `,
})
export class EspecieForm implements OnInit {
  /** Parametro :id de la ruta (withComponentInputBinding). */
  readonly id = input<string>();

  private readonly catalogo = inject(CatalogoService);
  private readonly router = inject(Router);
  protected readonly form = inject(FormBuilder).nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    descripcion: [''],
  });
  protected readonly error = signal<string | null>(null);
  protected readonly guardando = signal(false);

  ngOnInit(): void {
    const id = this.id();
    if (id) {
      this.catalogo.obtenerEspecie(+id).subscribe({
        next: (e) => this.form.patchValue({ nombre: e.nombre, descripcion: e.descripcion ?? '' }),
        error: (err) => this.error.set(mensajeError(err, 'cargar la especie')),
      });
    }
  }

  protected guardar(): void {
    this.guardando.set(true);
    this.error.set(null);
    const id = this.id();
    const peticion = id
      ? this.catalogo.actualizarEspecie(+id, this.form.getRawValue())
      : this.catalogo.crearEspecie(this.form.getRawValue());
    peticion.subscribe({
      next: () => this.router.navigateByUrl('/especies'),
      error: (err) => {
        this.error.set(mensajeError(err, 'guardar la especie'));
        this.guardando.set(false);
      },
    });
  }
}
