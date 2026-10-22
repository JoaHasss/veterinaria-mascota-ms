import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { mensajeError } from '../../core/http-error';
import { CatalogoService, Insumo, Mascota } from '../catalogo/catalogo-service';
import { CitaService, MetodoPago } from './cita-service';

@Component({
  selector: 'app-cita-crear',
  imports: [ReactiveFormsModule],
  template: `
    <h2>Nueva cita</h2>
    <form [formGroup]="form" (ngSubmit)="agendar()" class="formulario">
      <label>Mascota
        <select formControlName="mascotaId">
          <option [ngValue]="null" disabled>Selecciona...</option>
          @for (m of mascotas(); track m.id) {
            <option [ngValue]="m.id">{{ m.nombre }} ({{ m.especie.nombre }})</option>
          }
        </select>
      </label>
      <label>Motivo <input formControlName="motivo" maxlength="255" /></label>
      <label>Fecha y hora <input type="datetime-local" formControlName="fechaHora" /></label>
      <label>Monto (S/) <input type="number" min="0.01" step="0.01" formControlName="monto" /></label>
      <label>Método de pago
        <select formControlName="metodoPago">
          <option value="YAPE">Yape</option>
          <option value="TARJETA">Tarjeta</option>
          <option value="EFECTIVO">Efectivo</option>
          <option value="TARJETA_RECHAZADA">Tarjeta rechazada (prueba)</option>
        </select>
      </label>
      <label>Insumo (opcional)
        <select formControlName="insumoId">
          <option [ngValue]="null">Ninguno</option>
          @for (i of insumos(); track i.id) {
            <option [ngValue]="i.id">{{ i.nombre }} · stock {{ i.stock }}</option>
          }
        </select>
      </label>
      @if (form.controls.insumoId.value) {
        <label>Cantidad de insumo <input type="number" min="1" formControlName="cantidadInsumo" /></label>
      }
      @if (error()) {
        <p class="error">{{ error() }}</p>
      }
      <div class="acciones">
        <button type="submit" [disabled]="form.invalid || enviando()">Agendar cita</button>
      </div>
      <p class="nota">
        El 201 solo confirma que la cita se registró. El pago se resuelve después, de forma
        asíncrona (Kafka): su resultado se ve en "Mis citas".
      </p>
    </form>
  `,
})
export class CitaCrear implements OnInit {
  private readonly catalogo = inject(CatalogoService);
  private readonly citas = inject(CitaService);
  private readonly router = inject(Router);

  protected readonly mascotas = signal<Mascota[]>([]);
  protected readonly insumos = signal<Insumo[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly enviando = signal(false);

  protected readonly form = inject(FormBuilder).group({
    mascotaId: [null as number | null, Validators.required],
    motivo: ['Consulta general', [Validators.required, Validators.maxLength(255)]],
    fechaHora: [this.manana(), Validators.required],
    monto: [80 as number | null, Validators.min(0.01)],
    metodoPago: ['YAPE' as MetodoPago, Validators.required],
    insumoId: [null as number | null],
    cantidadInsumo: [1 as number | null, Validators.min(1)],
  });

  ngOnInit(): void {
    this.catalogo.listarMascotas().subscribe({
      next: (lista) => this.mascotas.set(lista.filter((m) => m.activo)),
      error: (err) => this.error.set(mensajeError(err, 'cargar las mascotas')),
    });
    this.catalogo.listarInsumos().subscribe({
      next: (lista) => this.insumos.set(lista),
      error: () => this.insumos.set([]),
    });
  }

  protected agendar(): void {
    const v = this.form.getRawValue();
    this.enviando.set(true);
    this.error.set(null);
    this.citas
      .crear({
        mascotaId: v.mascotaId!,
        motivo: v.motivo!,
        fechaHora: `${v.fechaHora}:00`,
        monto: v.monto ?? undefined,
        metodoPago: v.metodoPago!,
        insumoId: v.insumoId ?? undefined,
        cantidadInsumo: v.insumoId ? (v.cantidadInsumo ?? 1) : undefined,
      })
      .subscribe({
        next: (cita) => this.router.navigate(['/mis-citas'], { queryParams: { nueva: cita.id } }),
        error: (err) => {
          this.error.set(mensajeError(err, 'agendar la cita'));
          this.enviando.set(false);
        },
      });
  }

  /** Valor por defecto del datetime-local: manana a las 10:00 (yyyy-MM-ddTHH:mm). */
  private manana(): string {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    const p = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T10:00`;
  }
}
