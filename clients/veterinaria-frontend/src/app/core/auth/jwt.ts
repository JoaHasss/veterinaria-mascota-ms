/**
 * Decodifica el payload de un JWT SIN validar la firma. Solo sirve para
 * pintar la interfaz (nombre, roles); quien valida de verdad es el Gateway.
 */
export function decodificarJwt<T = Record<string, unknown>>(token: string): T {
  const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
  const json = decodeURIComponent(
    atob(payload)
      .split('')
      .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
      .join(''),
  );
  return JSON.parse(json) as T;
}
