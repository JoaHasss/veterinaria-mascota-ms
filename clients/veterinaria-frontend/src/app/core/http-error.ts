import { HttpErrorResponse } from '@angular/common/http';

/** Traduce un error HTTP del Gateway a un mensaje para el usuario. */
export function mensajeError(err: unknown, accion: string): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) return `No se pudo ${accion}: el Gateway no responde o bloqueo CORS.`;
    if (err.status === 403) return `No se pudo ${accion}: tu rol no tiene permiso (403).`;
    if (err.status === 404) return `No se pudo ${accion}: el recurso no existe (404).`;
    const detalle = (err.error && (err.error.message || err.error.error)) || err.statusText;
    return `No se pudo ${accion} (${err.status}): ${detalle}`;
  }
  return `No se pudo ${accion}.`;
}
