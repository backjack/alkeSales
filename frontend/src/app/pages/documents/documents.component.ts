import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { SalesApiService, StoredDocument } from '../../core/sales-api.service';

@Component({
  selector: 'app-documents', standalone: true, imports: [CommonModule, FormsModule],
  templateUrl: './documents.component.html', styleUrl: './documents.component.scss'
})
export class DocumentsComponent implements OnInit {
  private readonly api = inject(SalesApiService);
  readonly months = [
    { value: 4, label: 'April' }, { value: 5, label: 'May' }, { value: 6, label: 'June' },
    { value: 7, label: 'July' }, { value: 8, label: 'August' }, { value: 9, label: 'September' },
    { value: 10, label: 'October' }, { value: 11, label: 'November' }, { value: 12, label: 'December' },
    { value: 1, label: 'January' }, { value: 2, label: 'February' }, { value: 3, label: 'March' }
  ];
  documentDate = this.localDate(new Date()); selectedFiles: File[] = [];
  selectedFinancialYear = this.financialYear(new Date()); selectedMonth = new Date().getMonth() + 1;
  documents: StoredDocument[] = []; configured = false; connected = false;
  loading = false; uploading = false; error = ''; message = '';

  ngOnInit() { this.refreshStatus(); this.loadDocuments(); }
  get uploadFinancialYear() { return this.financialYear(new Date(this.documentDate + 'T00:00:00')); }
  get destination() {
    const date = new Date(this.documentDate + 'T00:00:00');
    const month = date.toLocaleString('en', { month: 'short' });
    return `FY-${this.uploadFinancialYear}-${this.uploadFinancialYear + 1}/${month}-${String(date.getFullYear()).slice(-2)}`;
  }
  get years() { const current = this.financialYear(new Date()); return Array.from({ length: 8 }, (_, i) => current + 1 - i); }

  refreshStatus() { this.api.getDriveStatus().subscribe({ next: r => { this.configured = r.data.configured; this.connected = r.data.connected; }, error: () => this.error = 'Could not read Google Drive status.' }); }
  connect() { window.location.href = '/documents/google/connect'; }
  chooseFiles(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFiles = Array.from(input.files ?? []); this.error = ''; this.message = '';
  }
  removeFile(index: number) { this.selectedFiles.splice(index, 1); this.selectedFiles = [...this.selectedFiles]; }
  upload() {
    if (!this.selectedFiles.length) { this.error = 'Select at least one PDF or image.'; return; }
    this.uploading = true; this.error = ''; this.message = '';
    this.api.uploadDocuments(this.documentDate, this.selectedFiles).pipe(finalize(() => this.uploading = false)).subscribe({
      next: r => { this.message = `${r.data.length} file(s) uploaded to ${this.destination}.`; this.selectedFiles = []; this.selectedFinancialYear = this.uploadFinancialYear; this.selectedMonth = new Date(this.documentDate + 'T00:00:00').getMonth() + 1; this.loadDocuments(); },
      error: e => this.error = e.error?.detail || e.error?.message || 'Upload failed. Please try again.'
    });
  }
  loadDocuments() {
    this.loading = true; this.error = '';
    this.api.getDocuments(this.selectedFinancialYear, this.selectedMonth).pipe(finalize(() => this.loading = false)).subscribe({ next: r => this.documents = r.data, error: () => this.error = 'Could not load documents.' });
  }
  download(document: StoredDocument) { this.api.downloadDocument(document.id).subscribe(blob => this.save(blob, document.originalName)); }
  downloadMonth() {
    const name = `FY-${this.selectedFinancialYear}-${this.selectedFinancialYear + 1}-${this.months.find(m => m.value === this.selectedMonth)?.label}.zip`;
    this.api.downloadDocumentMonth(this.selectedFinancialYear, this.selectedMonth).subscribe({ next: blob => this.save(blob, name), error: () => this.error = 'No files are available to download for this month.' });
  }
  formatSize(bytes: number) { return bytes < 1024 * 1024 ? `${Math.ceil(bytes / 1024)} KB` : `${(bytes / 1024 / 1024).toFixed(1)} MB`; }
  private save(blob: Blob, name: string) { const url = URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = name; a.click(); URL.revokeObjectURL(url); }
  private financialYear(date: Date) { return date.getMonth() >= 3 ? date.getFullYear() : date.getFullYear() - 1; }
  private localDate(date: Date) { return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`; }
}
