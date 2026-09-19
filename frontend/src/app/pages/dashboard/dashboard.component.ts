import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { MonthlySales, ClientSales, Option, Overview, SaleSummary, SalesApiService } from '../../core/sales-api.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard', standalone: true, imports: [CommonModule, FormsModule, RouterLink],
  template: `
  <section class="page">
    <div class="page-heading"><div><p class="eyebrow">Overview</p><h1>Invoice dashboard</h1><p>Track billing performance and client revenue.</p></div><div class="panel-actions"><button class="secondary" (click)="refresh()">Refresh data</button><a class="primary" routerLink="/invoices" [queryParams]="{create: '1'}">Create invoice</a></div></div>
    <div class="fy-loading" *ngIf="fyLoading"><span></span>Loading financial years…</div>
    <div class="filter-bar" *ngIf="!fyLoading">
      <label>Financial year<select [(ngModel)]="selectedYear" (ngModelChange)="refresh()"><option *ngFor="let year of years" [ngValue]="year.key">{{year.value}}</option></select></label>
      <label>Client<select [(ngModel)]="selectedClient" (ngModelChange)="refresh()"><option [ngValue]="0">All clients</option><option *ngFor="let client of clients" [ngValue]="client.key">{{client.value}}</option></select></label>
      <span class="filter-note">Updated from live sales records</span>
    </div>
    <div class="loading" *ngIf="loading">Loading dashboard…</div>
    <div class="error" *ngIf="error">{{error}}</div>
    <div class="metric-grid" *ngIf="!loading">
      <article class="metric"><span class="metric-icon blue">▤</span><div><small>Invoices created</small><strong>{{overview.invoiceCount || invoices.length}}</strong><em>Selected period</em></div></article>
      <article class="metric"><span class="metric-icon pink">₹</span><div><small>Total revenue</small><strong>{{overview.sumBill | currency:'INR':'symbol':'1.0-0'}}</strong><em>Including tax</em></div></article>
      <article class="metric"><span class="metric-icon amber">↗</span><div><small>Revenue after tax</small><strong>{{overview.netRevenue | currency:'INR':'symbol':'1.0-0'}}</strong><em>Net taxable value</em></div></article>
      <article class="metric"><span class="metric-icon green">✓</span><div><small>Payments received</small><strong>{{overview.sumPartPymt | currency:'INR':'symbol':'1.0-0'}}</strong><em>{{collectionRate | number:'1.0-0'}}% collected</em></div></article>
    </div>
    <div class="dashboard-grid">
      <article class="panel chart-panel"><div class="panel-head"><div><h2>Invoice revenue by month</h2><p>Invoice revenue across the selected financial year (April to March)</p></div></div>
        <div class="bars" *ngIf="monthRows.length; else emptyChart"><div class="bar-row" *ngFor="let row of monthRows"><span title="{{row.name}}">{{row.name}}</span><div class="bar-track"><i [style.width.%]="row.percent"></i></div><strong>{{row.revenue | currency:'INR':'symbol':'1.0-0'}}</strong></div></div>
        <ng-template #emptyChart><p class="empty">No client revenue found.</p></ng-template>
      </article>
      <article class="panel"><div class="panel-head"><div><h2>Invoice count by client</h2><p>Distribution across customers</p></div></div>
        <div class="client-counts"><div *ngFor="let row of invoiceCounts"><span class="dot"></span><span>{{row.name}}</span><strong>{{row.count}}</strong></div></div>
      </article>
    </div>
    <article class="panel recent"><div class="panel-head"><div><h2>Recent invoices</h2><p>Latest invoices matching the filters</p></div><a href="/invoices">View all</a></div>
      <div class="table-wrap"><table><thead><tr><th>Invoice</th><th>Client</th><th>Date</th><th>FY</th><th>Revenue</th><th>Status</th></tr></thead><tbody><tr *ngFor="let invoice of recentInvoices"><td><b>{{invoice.invoiceId}}</b></td><td>{{invoice.clientName}}</td><td>{{invoice.invoiceDate | date:'dd MMM yyyy'}}</td><td>{{invoice.fyYear}}</td><td>{{invoice.totalAmt | currency:'INR':'symbol':'1.0-0'}}</td><td><span class="status" [class.paid]="invoice.payReceived">{{invoice.payReceived ? 'Paid' : 'Pending'}}</span></td></tr></tbody></table></div>
    </article>
  </section>`
})
export class DashboardComponent implements OnInit {
  private readonly changeDetector = inject(ChangeDetectorRef); private readonly api = inject(SalesApiService);
  years: Option[] = []; clients: Option[] = []; invoices: SaleSummary[] = [];
  selectedYear = 0; selectedClient = 0; loading = true; fyLoading = true; error = '';
  overview: Overview & { netRevenue: number } = { sumBill: 0, sumPartPymt: 0, invoiceCount: 0, netRevenue: 0 };
  clientSales?: ClientSales; monthlySales?: MonthlySales;
  ngOnInit() {
    const current = this.api.currentFinancialYearKey();
    this.api.getYears().pipe(finalize(() => this.changeDetector.markForCheck())).subscribe({ next: response => { this.years = response.data ?? []; this.selectedYear = this.years.find(year => year.key === current)?.key ?? this.years.at(-1)?.key ?? this.years[0]?.key ?? current; this.fyLoading = false; this.refresh(); }, error: () => { this.error = 'Could not load financial years.'; this.fyLoading = false; this.loading = false; } });
    this.api.getClientOptions().pipe(finalize(() => this.changeDetector.markForCheck())).subscribe({ next: response => this.clients = response.data ?? [], error: () => this.error = 'Could not load clients.' });
  }
  refresh() { if (!this.selectedYear) return; this.loading = true; this.error = ''; const ids = this.selectedClient ? [this.selectedClient] : []; forkJoin({ overview: this.api.getOverview(this.selectedYear, ids), invoices: this.api.getInvoices([this.selectedYear], ids), clients: this.api.getClientSales(this.selectedYear), monthly: this.api.getMonthlySales(this.selectedYear) }).pipe(finalize(() => this.changeDetector.markForCheck())).subscribe({ next: data => { this.overview = {...data.overview.data, netRevenue: data.overview.data.netRevenue ?? 0}; this.invoices = data.invoices.data ?? []; this.clientSales = data.clients.data; this.monthlySales = data.monthly.data; this.loading = false; }, error: () => { this.error = 'Dashboard data could not be loaded. Please sign in again or retry.'; this.loading = false; } }); }
  get collectionRate() { return this.overview.sumBill ? (this.overview.sumPartPymt / this.overview.sumBill) * 100 : 0; }
  get recentInvoices() { return [...this.invoices].sort((a,b) => +new Date(b.invoiceDate) - +new Date(a.invoiceDate)).slice(0, 6); }
  get invoiceCounts() { const counts = new Map<string, number>(); this.invoices.forEach(x => counts.set(x.clientName, (counts.get(x.clientName) ?? 0) + 1)); return [...counts].map(([name,count]) => ({name,count})).sort((a,b) => b.count-a.count).slice(0,8); }
  get clientRows() { const labels = this.clientSales?.clientName ?? []; const values = this.clientSales?.dataSets?.[0]?.data ?? []; const max = Math.max(...values, 1); return labels.map((name, i) => ({name, revenue: values[i] ?? 0, percent: ((values[i] ?? 0) / max) * 100})).filter(x => !this.selectedClient || x.name === this.clients.find(c => c.key === this.selectedClient)?.value).sort((a,b) => b.revenue-a.revenue).slice(0,8); }
  get monthRows() { const values = this.monthlySales?.dataSets?.find(x => x.label.toLowerCase() === 'sales')?.data ?? []; const names=['Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec','Jan','Feb','Mar']; const max=Math.max(...values,1); return names.map((name,i)=>({name,revenue:values[i]??0,percent:((values[i]??0)/max)*100})).filter(x=>x.revenue>0); }
}
