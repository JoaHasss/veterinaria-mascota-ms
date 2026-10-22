import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnDestroy, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';
import { mensajeError } from '../../core/http-error';
import { Pago, PagoService } from '../pagos/pago-service';
import { Cita, CitaService, EstadoCita } from './cita-service';

interface CitaConPago {
  cita: Cita;
  pago: Pago | null;
  /** Consultas hechas mientras la cita seguia PENDIENTE_PAGO. */
  intentos: number;
  /** Se agoto pollingMaxIntentos sin cambio: se deja de sondear y se avisa. */
  demorada: boolean;
}

/**
 * "Mis citas": la pantalla que refleja la Saga de S9. POST /citas responde
 * 201 con la cita PENDIENTE_PAGO, pero el pago lo decide veterinaria-pago-ms
 * despues, al consumir cita.creada de Kafka. Estrategia elegida: polling
 * acotado. Cada pollingMs se vuelve a pedir SOLO las citas PENDIENTE_PAGO;
 * al cambiar de estado se deja de sondear esa cita y se consulta su pago.
 */
@Component({
  selector: 'app-mis-citas',
  imports: [RouterLink, DatePipe, DecimalPipe],
  template: `
    <div class="cabecera">
      <h2>Mis citas</h2>
      <a routerLink="/citas/nueva" class="boton">Nueva cita</a>
    </div>
    @if (sondeando()) {
      <p class="nota">⟳ Consultando citas pendientes cada {{ intervalo / 1000 }} s...</p>
    }
    @if (error()) {
      <p class="error">{{ error() }}</p>
    }
    @if (cargando()) {
      <p>Cargando...</p>
    } @else if (items().length === 0) {
      <p>Aún no tienes citas. <a routerLink="/citas/nueva">Agenda la primera</a>.</p>
    } @else {
      <table class="tabla">
        <thead>
          <tr><th>Cita</th><th>Mascota</th><th>Fecha</th><th>Monto</th><th>Método</th><th>Estado</th><th>Pago</th></tr>
        </thead>
        <tbody>
          @for (item of items(); track item.cita.id) {
            <tr [class.resaltada]="item.cita.id === +(nueva() ?? 0)">
              <td>#{{ item.cita.id }}</td>
              <td>{{ item.cita.mascotaNombre }}</td>
              <td>{{ item.cita.fechaHora | date: 'dd/MM/yyyy HH:mm' }}</td>
              <td>{{ item.cita.monto != null ? 'S/ ' + (item.cita.monto | number: '1.2-2') : '—' }}</td>
              <td>{{ item.cita.metodoPago ?? '—' }}</td>
              <td>
                <span class="estado" [class]="'estado ' + item.cita.estado">{{ etiqueta(item.cita.estado) }}</span>
                @if (item.demorada) {
                  <small class="aviso">Demora inusual: el pago no se ha resuelto. Revisa más tarde.</small>
                }
              </td>
              <td>
                @if (item.pago) {
                  #{{ item.pago.id }} · {{ item.pago.estado }}
                } @else if (item.cita.estado === 'PENDIENTE_PAGO') {
                  <em>todavía no se sabe</em>
                } @else {
                  —
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class MisCitas implements OnInit, OnDestroy {
  /** ?nueva=<id>: resalta la cita recien creada. */
  readonly nueva = input<string>();

  private readonly citas = inject(CitaService);
  private readonly pagos = inject(PagoService);

  protected readonly intervalo = environment.pollingMs;
  protected readonly items = signal<CitaConPago[]>([]);
  protected readonly cargando = signal(true);
  protected readonly sondeando = signal(false);
  protected readonly error = signal<string | null>(null);
  private temporizador: ReturnType<typeof setInterval> | null = null;

  ngOnInit(): void {
    this.citas.listar().subscribe({
      next: (lista) => {
        const ordenadas = [...lista].sort((a, b) => b.id - a.id);
        this.items.set(ordenadas.map((cita) => ({ cita, pago: null, intentos: 0, demorada: false })));
        this.cargando.set(false);
        ordenadas.filter((c) => c.estado !== 'PENDIENTE_PAGO').forEach((c) => this.cargarPago(c.id));
        this.programarSondeo();
      },
      error: (err) => {
        this.error.set(mensajeError(err, 'cargar tus citas'));
        this.cargando.set(false);
      },
    });
  }

  /** Sin esto el intervalo seguiria pidiendo al Gateway despues de salir de la pantalla. */
  ngOnDestroy(): void {
    this.detenerSondeo();
  }

  protected etiqueta(estado: EstadoCita): string {
    switch (estado) {
      case 'PENDIENTE_PAGO': return 'Procesando pago…';
      case 'PAGADA': return 'Pagada';
      case 'CANCELADA': return 'Pago rechazado (cita cancelada)';
      case 'PENDIENTE_INSUMO': return 'Sin stock del insumo';
      case 'PENDIENTE_VALIDACION': return 'Validando mascota';
      default: return estado;
    }
  }

  private programarSondeo(): void {
    if (this.temporizador || !this.hayPendientes()) return;
    this.sondeando.set(true);
    this.temporizador = setInterval(() => this.sondear(), this.intervalo);
  }

  private sondear(): void {
    const pendientes = this.items().filter((i) => i.cita.estado === 'PENDIENTE_PAGO' && !i.demorada);
    if (pendientes.length === 0) {
      this.detenerSondeo();
      return;
    }
    for (const item of pendientes) {
      console.debug(`[polling] GET /api/v1/citas/${item.cita.id} (intento ${item.intentos + 1})`);
      this.citas.obtener(item.cita.id).subscribe({
        next: (cita) => {
          const intentos = item.intentos + 1;
          const demorada = cita.estado === 'PENDIENTE_PAGO' && intentos >= environment.pollingMaxIntentos;
          this.actualizar(cita.id, { cita, intentos, demorada });
          if (cita.estado !== 'PENDIENTE_PAGO') {
            console.info(`[polling] cita ${cita.id}: PENDIENTE_PAGO -> ${cita.estado}, se deja de sondear`);
            this.cargarPago(cita.id);
          }
        },
        error: (err) => this.error.set(mensajeError(err, `actualizar la cita ${item.cita.id}`)),
      });
    }
  }

  private cargarPago(citaId: number): void {
    this.pagos.obtenerPorCita(citaId).subscribe({
      next: (pago) => this.actualizar(citaId, { pago }),
      error: () => this.actualizar(citaId, { pago: null }),
    });
  }

  private actualizar(citaId: number, cambios: Partial<CitaConPago>): void {
    this.items.update((lista) => lista.map((i) => (i.cita.id === citaId ? { ...i, ...cambios } : i)));
    if (!this.hayPendientes()) this.detenerSondeo();
  }

  private hayPendientes(): boolean {
    return this.items().some((i) => i.cita.estado === 'PENDIENTE_PAGO' && !i.demorada);
  }

  private detenerSondeo(): void {
    if (this.temporizador) {
      clearInterval(this.temporizador);
      this.temporizador = null;
      console.info('[polling] detenido');
    }
    this.sondeando.set(false);
  }
}
