import { Routes } from '@angular/router';
import { AuthCallback } from './core/auth/auth-callback';
import { authGuard } from './core/guards/auth-guard';
import { rolGuard } from './core/guards/rol-guard';
import { Inicio } from './core/inicio/inicio';
import { Layout } from './core/layout/layout';
import { EspecieForm } from './features/catalogo/especie-form';
import { EspecieList } from './features/catalogo/especie-list';
import { CitaCrear } from './features/citas/cita-crear';
import { MisCitas } from './features/citas/mis-citas';
import { PagoList } from './features/pagos/pago-list';

export const routes: Routes = [
  { path: 'auth/callback', component: AuthCallback },
  {
    path: '',
    component: Layout,
    children: [
      { path: '', component: Inicio },
      { path: 'especies', component: EspecieList, canActivate: [authGuard] },
      { path: 'especies/nueva', component: EspecieForm, canActivate: [authGuard, rolGuard('ADMIN')] },
      { path: 'especies/:id/editar', component: EspecieForm, canActivate: [authGuard, rolGuard('ADMIN')] },
      { path: 'citas/nueva', component: CitaCrear, canActivate: [authGuard, rolGuard('CLIENTE')] },
      { path: 'mis-citas', component: MisCitas, canActivate: [authGuard, rolGuard('CLIENTE')] },
      { path: 'pagos', component: PagoList, canActivate: [authGuard, rolGuard('ADMIN')] },
    ],
  },
  { path: '**', redirectTo: '' },
];
