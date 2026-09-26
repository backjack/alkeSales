import { Component, DestroyRef, HostListener, inject } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-shell">
      <button class="sidebar-backdrop" [class.open]="menuOpen" (click)="closeMenu()" aria-label="Close navigation menu" tabindex="-1"></button>
      <aside class="sidebar" [class.open]="menuOpen" id="main-navigation">
        <div class="sidebar-head"><a class="brand" routerLink="/dashboard" (click)="closeMenu()"><span class="brand-mark">A</span><span>ALKE</span></a><button class="sidebar-close icon-button" (click)="closeMenu()" aria-label="Close menu" title="Close menu">×</button></div>
        <nav aria-label="Main navigation">
          <a routerLink="/dashboard" routerLinkActive="active" (click)="closeMenu()"><span>⌂</span> Dashboard</a>
          <a routerLink="/invoices" routerLinkActive="active" (click)="closeMenu()"><span>▤</span> Invoices</a>
          <a routerLink="/clients" routerLinkActive="active" (click)="closeMenu()"><span>♙</span> Clients</a>
          <a routerLink="/analytics" routerLinkActive="active" (click)="closeMenu()"><span>⌁</span> Analytics</a>
        </nav>
        <div class="sidebar-foot"><span class="avatar">NS</span><div><strong>Sales Admin</strong><small>ALKE Finance</small></div></div>
      </aside>
      <main class="main-content">
        <header class="topbar"><button class="mobile-menu" (click)="toggleMenu()" [attr.aria-expanded]="menuOpen" aria-controls="main-navigation" [attr.aria-label]="menuOpen ? 'Close menu' : 'Open menu'">☰</button><div class="top-actions"><button title="Notifications">○</button><a href="/logout">Sign out</a></div></header>
        <router-outlet />
      </main>
    </div>`
})
export class AppComponent {
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  menuOpen = false;

  constructor() {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(() => this.closeMenu());
  }

  toggleMenu() { this.menuOpen = !this.menuOpen; }
  closeMenu() { this.menuOpen = false; }

  @HostListener('document:keydown.escape')
  onEscape() { this.closeMenu(); }

  @HostListener('window:resize')
  onResize() { if (window.innerWidth > 720) this.closeMenu(); }
}
