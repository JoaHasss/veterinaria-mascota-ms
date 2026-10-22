import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type MetodoPago = 'TARJETA' | 'YAPE' | 'EFECTIVO' | 'TARJETA_RECHAZADA';

export type EstadoCita =
  | 'PENDIENTE_PAGO'
  | 'PAGADA'
  | 'CANCELADA'
  | 'PENDIENTE_INSUMO'
  | 'PENDIENTE_VALIDACION';

export interface CitaRequest {
  mascotaId: number;
  motivo: string;
  fechaHora: string;
  monto?: number;
  metodoPago: MetodoPago;
  insumoId?: number;
  cantidadInsumo?: number;
}

export interface Cita {
  id: number;
  idCliente: number;
  mascotaId: number;
  mascotaNombre: string;
  motivo: string;
  fechaHora: string;
  estado: EstadoCita;
  /** Puede venir null en citas anteriores a S8. */
  monto: number | null;
  metodoPago: MetodoPago | null;
  insumoId: number | null;
  cantidadInsumo: number | null;
}

/** citas-ms a traves del Gateway. El idCliente nunca se envia: citas-ms lo toma del JWT. */
@Injectable({ providedIn: 'root' })
export class CitaService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiBaseUrl}/api/v1/citas`;

  crear(cita: CitaRequest): Observable<Cita> {
    return this.http.post<Cita>(this.api, cita);
  }

  obtener(id: number): Observable<Cita> {
    return this.http.get<Cita>(`${this.api}/${id}`);
  }

  /** citas-ms ya filtra por el idCliente del token: devuelve solo las propias. */
  listar(): Observable<Cita[]> {
    return this.http.get<Cita[]>(this.api);
  }
}
