import { Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { InvoicesComponent } from './pages/invoices/invoices.component';
import { ClientsComponent } from './pages/clients/clients.component';
import { AnalyticsComponent } from './pages/analytics/analytics.component';

export const routes: Routes = [
  { path: 'dashboard', component: DashboardComponent, title: 'Dashboard · ALKE' },
  { path: 'analytics', component: AnalyticsComponent, title: 'Revenue Analytics · ALKE' },
  { path: 'invoices', component: InvoicesComponent, title: 'Invoices · ALKE' },
  { path: 'clients', component: ClientsComponent, title: 'Clients · ALKE' },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: '**', redirectTo: 'dashboard' }
];
