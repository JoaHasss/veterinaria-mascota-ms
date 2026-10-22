import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Especie {
  id?: number;
  nombre: string;
  descripcion?: string;
}

export interface Mascota {
  id: number;
  nombre: string;
  descripcion?: string;
  peso: number;
  activo: boolean;
  especie: Especie;
}

export interface Insumo {
  id: number;
  nombre: string;
  tipo: string;
  stock: number;
}

/** veterinaria-mascota-ms a traves del Gateway: especies, mascotas e insumos. */
@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiBaseUrl}/api/v1`;

  listarEspecies(): Observable<Especie[]> {
    return this.http.get<Especie[]>(`${this.api}/especies`);
  }

  obtenerEspecie(id: number): Observable<Especie> {
    return this.http.get<Especie>(`${this.api}/especies/${id}`);
  }

  crearEspecie(especie: Especie): Observable<Especie> {
    return this.http.post<Especie>(`${this.api}/especies`, especie);
  }

  actualizarEspecie(id: number, especie: Especie): Observable<Especie> {
    return this.http.put<Especie>(`${this.api}/especies/${id}`, especie);
  }

  eliminarEspecie(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/especies/${id}`);
  }

  listarMascotas(): Observable<Mascota[]> {
    return this.http.get<Mascota[]>(`${this.api}/mascotas`);
  }

  listarInsumos(): Observable<Insumo[]> {
    return this.http.get<Insumo[]>(`${this.api}/insumos`);
  }
}
