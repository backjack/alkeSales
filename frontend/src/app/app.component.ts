import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-shell">
      <aside class="sidebar">
        <a class="brand" routerLink="/dashboard"><span class="brand-mark">A</span><span>ALKE</span></a>
        <nav aria-label="Main navigation">
          <a routerLink="/dashboard" routerLinkActive="active"><span>⌂</span> Dashboard</a>
          <a routerLink="/invoices" routerLinkActive="active"><span>▤</span> Invoices</a>
          <a routerLink="/clients" routerLinkActive="active"><span>♙</span> Clients</a>
        </nav>
        <div class="sidebar-foot"><span class="avatar">NS</span><div><strong>Sales Admin</strong><small>ALKE Finance</small></div></div>
      </aside>
      <main class="main-content">
        <header class="topbar"><button class="mobile-menu" aria-label="Open menu">☰</button><div class="top-actions"><button title="Notifications">○</button><a href="/logout">Sign out</a></div></header>
        <router-outlet />
      </main>
    </div>`
})
export class AppComponent {}
