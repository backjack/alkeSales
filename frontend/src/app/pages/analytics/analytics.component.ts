import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { MonthlySales, Option, SalesApiService } from '../../core/sales-api.service';

interface ChartPoint { x: number; y: number; value: number; }
interface ChartSeries { label: string; color: string; values: number[]; points: ChartPoint[]; path: string; }
interface ChartModel { max: number; empty: boolean; series: ChartSeries[]; }

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './analytics.component.html',
  styleUrl: './analytics.component.scss'
})
export class AnalyticsComponent implements OnInit {
  private readonly api = inject(SalesApiService);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly palette = ['#6f4bea', '#1689ca', '#e56b6f', '#e49a24', '#1d9b74', '#8c5aa8', '#3c6fdd', '#b8672d'];

  readonly months = ['Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec', 'Jan', 'Feb', 'Mar'];
  readonly gridLines = [0, 1, 2, 3, 4];
  years: Option[] = [];
  selectedYear = 0;
  comparisonMetric: 'total' | 'net' = 'total';
  comparisonView: 'monthly' | 'cumulative' = 'monthly';
  comparisonYears = new Set<number>();
  loading = true;
  error = '';
  monthlyChart: ChartModel = this.emptyChart();
  cumulativeChart: ChartModel = this.emptyChart();
  comparisonChart: ChartModel = this.emptyChart();
  totalRevenue = 0;
  netRevenue = 0;
  private monthlyByYear = new Map<number, MonthlySales>();

  ngOnInit() {
    const current = this.api.currentFinancialYearKey();
    this.api.getYears().subscribe({
      next: response => {
        this.years = [...(response.data ?? [])].sort((a, b) => a.key - b.key);
        this.selectedYear = this.years.find(year => year.key === current)?.key ?? this.years.at(-1)?.key ?? current;
        this.comparisonYears = new Set(this.years.map(year => year.key));
        this.refresh();
      },
      error: () => {
        this.error = 'Could not load financial years.';
        this.loading = false;
        this.changeDetector.markForCheck();
      }
    });
  }

  refresh() {
    if (!this.years.length) return;
    this.loading = true;
    this.error = '';
    forkJoin(this.years.map(year => this.api.getMonthlySales(year.key)))
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: responses => {
          this.monthlyByYear.clear();
          responses.forEach((response, index) => this.monthlyByYear.set(this.years[index].key, response.data));
          this.buildCharts();
          this.loading = false;
        },
        error: () => {
          this.error = 'Analytics data could not be loaded. Please sign in again or retry.';
          this.loading = false;
        }
      });
  }

  buildCharts() {
    const total = this.values(this.selectedYear, 'sales');
    const net = this.values(this.selectedYear, 'netRevenue');
    this.totalRevenue = total.reduce((sum, value) => sum + value, 0);
    this.netRevenue = net.reduce((sum, value) => sum + value, 0);
    this.monthlyChart = this.createChart([
      { label: 'Total revenue', color: '#6f4bea', values: total },
      { label: 'Revenue post tax', color: '#1689ca', values: net }
    ]);
    this.cumulativeChart = this.createChart([
      { label: 'Cumulative total revenue', color: '#6f4bea', values: this.cumulative(total) },
      { label: 'Cumulative revenue post tax', color: '#1689ca', values: this.cumulative(net) }
    ]);
    this.buildComparisonChart();
  }

  buildComparisonChart() {
    const dataSet = this.comparisonMetric === 'total' ? 'sales' : 'netRevenue';
    this.comparisonChart = this.createChart(this.years.filter(year => this.comparisonYears.has(year.key)).map((year, index) => ({
      label: year.value,
      color: this.palette[index % this.palette.length],
      values: this.comparisonView === 'cumulative' ? this.cumulative(this.values(year.key, dataSet)) : this.values(year.key, dataSet)
    })));
  }

  toggleComparisonYear(year: number, selected: boolean) {
    if (selected) this.comparisonYears.add(year);
    else this.comparisonYears.delete(year);
    this.buildComparisonChart();
  }

  get selectedYearLabel() { return this.years.find(year => year.key === this.selectedYear)?.value ?? this.selectedYear; }
  get taxDifference() { return Math.max(0, this.totalRevenue - this.netRevenue); }

  x(index: number) { return 70 + (index * 900 / 11); }
  gridY(index: number) { return 24 + (index * 282 / 4); }
  gridValue(chart: ChartModel, index: number) { return chart.max * (4 - index) / 4; }
  money(value: number) {
    const absolute = Math.abs(value);
    if (absolute >= 10_000_000) return `₹${(value / 10_000_000).toFixed(1)}Cr`;
    if (absolute >= 100_000) return `₹${(value / 100_000).toFixed(1)}L`;
    if (absolute >= 1_000) return `₹${(value / 1_000).toFixed(0)}K`;
    return `₹${Math.round(value)}`;
  }

  private values(year: number, label: string) {
    const data = this.monthlyByYear.get(year)?.dataSets?.find(item => item.label.toLowerCase() === label.toLowerCase())?.data ?? [];
    return Array.from({ length: 12 }, (_, index) => Number(data[index]) || 0);
  }

  private cumulative(values: number[]) {
    let running = 0;
    return values.map(value => running += value);
  }

  private createChart(input: { label: string; color: string; values: number[] }[]): ChartModel {
    const max = Math.max(0, ...input.flatMap(series => series.values));
    const scaleMax = max || 1;
    const series = input.map(item => {
      const points = item.values.map((value, index) => ({ x: this.x(index), y: 306 - (value / scaleMax * 282), value }));
      return { ...item, points, path: points.map((point, index) => `${index ? 'L' : 'M'} ${point.x} ${point.y}`).join(' ') };
    });
    return { max, empty: max === 0, series };
  }

  private emptyChart(): ChartModel { return { max: 0, empty: true, series: [] }; }
}
