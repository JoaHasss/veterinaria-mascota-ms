// Authorization Code + PKCE (RFC 7636) con Web Crypto: sin librerias externas.

function base64Url(bytes: Uint8Array): string {
  let binario = '';
  bytes.forEach((b) => (binario += String.fromCharCode(b)));
  return btoa(binario).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

function aleatorio(longitud: number): string {
  const bytes = new Uint8Array(longitud);
  crypto.getRandomValues(bytes);
  return base64Url(bytes);
}

/** Secreto de un solo uso que nunca sale del navegador hasta el canje del codigo. */
export function generarCodeVerifier(): string {
  return aleatorio(32);
}

/** SHA-256 del verifier en Base64URL: es lo unico que viaja en la URL de /auth. */
export async function generarCodeChallenge(verifier: string): Promise<string> {
  const hash = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(verifier));
  return base64Url(new Uint8Array(hash));
}

/** Valor anti-CSRF: Keycloak lo devuelve tal cual y la SPA lo compara al volver. */
export function generarState(): string {
  return aleatorio(16);
}
