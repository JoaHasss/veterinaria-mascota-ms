import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { mensajeError } from '../../core/http-error';
import { Pago, PagoService } from './pago-service';

/** Solo ADMIN (rolGuard en la ruta y hasRole("ADMIN") en el Gateway). */
@Component({
  selector: 'app-pago-list',
  imports: [DatePipe, DecimalPipe],
  template: `
    <h2>Pagos</h2>
    @if (cargando()) {
      <p>Cargando...</p>
    } @else if (error()) {
      <p class="error">{{ error() }}</p>
    } @else {
      <table class="tabla">
        <thead><tr><th>Pago</th><th>Cita</th><th>Cliente</th><th>Monto</th><th>Método</th><th>Estado</th><th>Fecha</th></tr></thead>
        <tbody>
          @for (p of pagos(); track p.id) {
            <tr>
              <td>#{{ p.id }}</td>
              <td>#{{ p.citaId }}</td>
              <td>{{ p.idCliente ?? '—' }}</td>
              <td>S/ {{ p.monto | number: '1.2-2' }}</td>
              <td>{{ p.metodoPago }}</td>
              <td><span class="estado" [class]="'estado ' + p.estado">{{ p.estado }}</span></td>
              <td>{{ p.fechaPago | date: 'dd/MM/yyyy HH:mm:ss' }}</td>
            </tr>
          } @empty {
            <tr><td colspan="7">No hay pagos registrados.</td></tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class PagoList implements OnInit {
  private readonly pagoService = inject(PagoService);
  protected readonly pagos = signal<Pago[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly cargando = signal(true);

  ngOnInit(): void {
    this.pagoService.listar().subscribe({
      next: (lista) => {
        this.pagos.set([...lista].sort((a, b) => b.id - a.id));
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeError(err, 'cargar los pagos'));
        this.cargando.set(false);
      },
    });
  }
}
