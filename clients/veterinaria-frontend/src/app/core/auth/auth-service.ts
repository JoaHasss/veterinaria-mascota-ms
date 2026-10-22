import { Injectable, computed, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { decodificarJwt } from './jwt';
import { generarCodeChallenge, generarCodeVerifier, generarState } from './pkce';

interface ClaimsKeycloak {
  sub: string;
  exp: number;
  preferred_username?: string;
  name?: string;
  email?: string;
  idCliente?: number;
  realm_access?: { roles?: string[] };
}

interface RespuestaToken {
  access_token: string;
  id_token?: string;
  expires_in: number;
}

const CLAVE_VERIFIER = 'pkce_code_verifier';
const CLAVE_STATE = 'pkce_state';
const CLAVE_DESTINO = 'pkce_destino';

/**
 * Cliente OIDC minimo para Keycloak (Authorization Code + PKCE).
 * Los tokens viven solo en memoria (signals): nunca en localStorage, donde
 * cualquier script inyectado (XSS) podria leerlos. Al recargar la pagina se
 * pierden y el guard vuelve a pedir login; como la sesion SSO de Keycloak
 * sigue viva, el usuario no vuelve a escribir su contrasena.
 * En sessionStorage solo quedan verifier, state y destino, y solo durante
 * el viaje de ida y vuelta a Keycloak.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly kc = environment.keycloak;
  private readonly endpoint = `${this.kc.issuer}/protocol/openid-connect`;

  private readonly accessToken = signal<string | null>(null);
  private readonly idToken = signal<string | null>(null);
  private readonly claims = signal<ClaimsKeycloak | null>(null);

  readonly autenticado = computed(() => {
    const c = this.claims();
    return c !== null && c.exp * 1000 > Date.now();
  });
  readonly usuario = computed(() => this.claims()?.preferred_username ?? null);
  readonly nombre = computed(() => this.claims()?.name ?? this.usuario());
  readonly roles = computed(() => this.claims()?.realm_access?.roles ?? []);
  /** sub de Keycloak (UUID): identifica la cuenta, no al cliente de la veterinaria. */
  readonly sub = computed(() => this.claims()?.sub ?? null);
  /** Claim de negocio puesto por el protocol mapper idCliente (user attribute). */
  readonly idCliente = computed(() => this.claims()?.idCliente ?? null);

  token(): string | null {
    return this.autenticado() ? this.accessToken() : null;
  }

  tieneRol(...roles: string[]): boolean {
    return roles.some((r) => this.roles().includes(r));
  }

  /** Paso 1: genera verifier/challenge/state y redirige a la pantalla de login de Keycloak. */
  async iniciarLogin(destino = '/'): Promise<void> {
    const verifier = generarCodeVerifier();
    const state = generarState();
    sessionStorage.setItem(CLAVE_VERIFIER, verifier);
    sessionStorage.setItem(CLAVE_STATE, state);
    sessionStorage.setItem(CLAVE_DESTINO, destino);

    const params = new URLSearchParams({
      client_id: this.kc.clientId,
      redirect_uri: this.kc.redirectUri,
      response_type: 'code',
      scope: 'openid profile',
      state,
      code_challenge: await generarCodeChallenge(verifier),
      code_challenge_method: 'S256',
    });
    window.location.assign(`${this.endpoint}/auth?${params}`);
  }

  /**
   * Paso 2 (ruta /auth/callback): verifica state y canjea el code por tokens
   * enviando el code_verifier. Devuelve la ruta a la que queria ir el usuario.
   * Usa fetch y no HttpClient a proposito: esta llamada va a Keycloak y no
   * debe pasar por los interceptores pensados para el Gateway.
   */
  async completarLogin(params: URLSearchParams): Promise<string> {
    const error = params.get('error');
    if (error) {
      throw new Error(`Keycloak respondio ${error}: ${params.get('error_description') ?? ''}`);
    }
    const code = params.get('code');
    const stateRecibido = params.get('state');
    const stateEsperado = sessionStorage.getItem(CLAVE_STATE);
    const verifier = sessionStorage.getItem(CLAVE_VERIFIER);
    const destino = sessionStorage.getItem(CLAVE_DESTINO) ?? '/';
    sessionStorage.removeItem(CLAVE_STATE);
    sessionStorage.removeItem(CLAVE_VERIFIER);
    sessionStorage.removeItem(CLAVE_DESTINO);

    if (!code || !verifier) {
      throw new Error('Falta el code o el code_verifier: inicia sesion de nuevo.');
    }
    if (!stateRecibido || stateRecibido !== stateEsperado) {
      console.warn('[auth] state NO coincide: se descarta la respuesta', { stateRecibido, stateEsperado });
      throw new Error('El parametro state no coincide: posible CSRF, se descarta la respuesta.');
    }
    console.info('[auth] state verificado', stateRecibido);

    const respuesta = await fetch(`${this.endpoint}/token`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        client_id: this.kc.clientId,
        code,
        redirect_uri: this.kc.redirectUri,
        code_verifier: verifier,
      }),
    });
    if (!respuesta.ok) {
      throw new Error(`Canje del codigo rechazado (${respuesta.status}): ${await respuesta.text()}`);
    }
    const tokens = (await respuesta.json()) as RespuestaToken;
    console.info('[auth] code canjeado con code_verifier; token valido por', tokens.expires_in, 's');
    this.guardar(tokens);
    return destino;
  }

  cerrarSesion(): void {
    const idToken = this.idToken();
    this.accessToken.set(null);
    this.idToken.set(null);
    this.claims.set(null);
    const params = new URLSearchParams({
      client_id: this.kc.clientId,
      post_logout_redirect_uri: this.kc.postLogoutRedirectUri,
    });
    if (idToken) {
      params.set('id_token_hint', idToken);
    }
    window.location.assign(`${this.endpoint}/logout?${params}`);
  }

  private guardar(tokens: RespuestaToken): void {
    this.accessToken.set(tokens.access_token);
    this.idToken.set(tokens.id_token ?? null);
    this.claims.set(decodificarJwt<ClaimsKeycloak>(tokens.access_token));
  }
}
