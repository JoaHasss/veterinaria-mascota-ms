import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Pago {
  id: number;
  citaId: number;
  idCliente: number | null;
  monto: number;
  metodoPago: string;
  estado: 'VALIDADO' | 'FALLIDO';
  fechaPago: string;
}

/** veterinaria-pago-ms a traves del Gateway (solo lectura). */
@Injectable({ providedIn: 'root' })
export class PagoService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiBaseUrl}/api/v1/pagos`;

  /** Solo ADMIN: el Gateway responde 403 a cualquier otro rol. */
  listar(): Observable<Pago[]> {
    return this.http.get<Pago[]>(this.api);
  }

  /**
   * 404 = el evento cita.creada aun no se procesa: no es un error, es
   * "todavia no se sabe". Se devuelve null para que la pantalla lo muestre asi.
   */
  obtenerPorCita(citaId: number): Observable<Pago | null> {
    return this.http.get<Pago>(`${this.api}/por-cita/${citaId}`).pipe(
      catchError((err: HttpErrorResponse) => (err.status === 404 ? of(null) : throwError(() => err))),
    );
  }
}
