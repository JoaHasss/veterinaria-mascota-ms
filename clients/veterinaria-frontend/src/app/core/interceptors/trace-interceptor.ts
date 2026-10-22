import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment';

/**
 * Cada peticion al Gateway lleva su propio X-Trace-ID. Los CorrelationIdFilter
 * de los microservicios lo ponen en el MDC, asi un clic en la SPA se puede
 * seguir en los logs de cada servicio (S6/S10).
 */
export const traceInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiBaseUrl)) {
    return next(req);
  }
  return next(req.clone({ setHeaders: { 'X-Trace-ID': crypto.randomUUID() } }));
};
