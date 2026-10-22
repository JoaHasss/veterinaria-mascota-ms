// S11: la SPA solo conoce dos direcciones: el Gateway (datos) y Keycloak (identidad).
export const environment = {
  apiBaseUrl: 'http://localhost:18080',
  keycloak: {
    issuer: 'http://localhost:18180/realms/veterinaria',
    clientId: 'veterinaria-frontend',
    redirectUri: 'http://localhost:4200/auth/callback',
    postLogoutRedirectUri: 'http://localhost:4200/',
  },
  // Mis citas: cada cuanto se vuelve a consultar una cita PENDIENTE_PAGO.
  pollingMs: 4000,
  // Tras este numero de consultas sin cambio se deja de sondear y se avisa al usuario.
  pollingMaxIntentos: 15,
};
