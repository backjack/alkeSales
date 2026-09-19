import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, HostListener, NgZone, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { AgGridAngular } from 'ag-grid-angular';
import { AllCommunityModule, ColDef, GetRowIdFunc, GridApi, GridReadyEvent, ICellRendererParams, ModuleRegistry, RowSelectionOptions, SelectionChangedEvent, themeQuartz } from 'ag-grid-community';
import { Client, InvoiceDetail, InvoiceItem, Option, SaleSummary, SalesApiService } from '../../core/sales-api.service';
import { ActivatedRoute, Router } from '@angular/router';
import { InvoiceItemCellComponent, ItemField } from './invoice-item-cell.component';

ModuleRegistry.registerModules([AllCommunityModule]);

@Component({
  selector: 'app-invoices', standalone: true, imports: [CommonModule, FormsModule, AgGridAngular, MatAutocompleteModule, MatDatepickerModule, MatNativeDateModule, MatFormFieldModule, MatGridListModule, MatInputModule, MatSelectModule],
  templateUrl: './invoices.component.html'
})
export class InvoicesComponent implements OnInit {
  private readonly api = inject(SalesApiService);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  years: Option[] = []; clients: Option[] = []; clientRecords: Client[] = []; itemRecords: InvoiceItem[] = []; invoices: SaleSummary[] = [];
  filteredInvoices: SaleSummary[] = [];
  clientFilterValue: Option | string = '';
  editorClientValue: Client | string = '';
  readonly months = ['January','February','March','April','May','June','July','August','September','October','November','December'];
  year = 0; client = 0; month = 0; query = ''; loading = false; fyLoading = true; downloading = false;
  error = ''; message = ''; selected = new Set<string>();
  editorOpen = false; detailLoading = false; saving = false; yearLoading = false; creating = false; editing = false;
  editorError = ''; detail?: InvoiceDetail; summary?: SaleSummary;
  invoiceDatePickerValue: Date | null = null;
  formColumns = 3;
  private gridApi?: GridApi<SaleSummary>;
  private itemGridApi?: GridApi<InvoiceItem>;
  private yearRequest = 0;
  readonly gridTheme = themeQuartz.withParams({
    accentColor: '#6f4bea', backgroundColor: '#ffffff', foregroundColor: '#263147',
    headerBackgroundColor: '#f7f8fc', headerTextColor: '#69758b', rowHoverColor: '#f6f3ff',
    fontFamily: "'DM Sans', Arial, sans-serif", fontSize: 13, headerFontSize: 11,
    headerFontWeight: 700, rowHeight: 50, headerHeight: 44, spacing: 6,
  });
  readonly defaultColDef: ColDef<SaleSummary> = { sortable: true, resizable: true, suppressHeaderMenuButton: true };
  readonly rowSelection: RowSelectionOptions<SaleSummary> = { mode: 'multiRow', checkboxes: true, headerCheckbox: true, selectAll: 'filtered', enableClickSelection: false };
  readonly getRowId: GetRowIdFunc<SaleSummary> = params => this.key(params.data);
  readonly gridColumns: ColDef<SaleSummary>[] = [
    { headerName: 'Invoice', field: 'invoiceId', minWidth: 150, width: 155, cellRenderer: (p: ICellRendererParams<SaleSummary>) => this.renderInvoiceLink(p) },
    { headerName: 'Client', field: 'clientName', minWidth: 190, flex: 1.6, tooltipField: 'clientName' },
    { headerName: 'Date', field: 'invoiceDate', minWidth: 135, width: 140, sort: 'desc', valueFormatter: p => this.formatGridDate(p.value) },
    { headerName: 'Total', field: 'totalAmt', minWidth: 125, width: 130, cellClass: 'invoice-grid-amount', valueFormatter: p => this.formatMoney(p.value) },
    { headerName: 'Comments', field: 'details', minWidth: 220, flex: 1.4, tooltipField: 'details', cellClass: 'invoice-grid-comment-cell', cellRenderer: (p: ICellRendererParams<SaleSummary>) => this.renderComment(p) },
    { headerName: 'Actions', minWidth: 195, width: 205, sortable: false, resizable: false, cellRenderer: (p: ICellRendererParams<SaleSummary>) => this.renderActions(p) },
  ];
  readonly itemGridTheme = themeQuartz.withParams({
    accentColor: '#6f4bea', backgroundColor: '#ffffff', foregroundColor: '#263147',
    headerBackgroundColor: '#f7f8fc', headerTextColor: '#69758b', rowHoverColor: '#f6f3ff',
    fontFamily: "'DM Sans', Arial, sans-serif", fontSize: 13, headerFontSize: 12,
    headerFontWeight: 700, rowHeight: 76, headerHeight: 56, spacing: 4,
  });
  readonly itemDefaultColDef: ColDef<InvoiceItem> = {
    sortable: true, filter: true, resizable: true, wrapHeaderText: true,
    suppressKeyboardEvent: p => p.event.target instanceof HTMLElement && !!p.event.target.closest('input, textarea'),
  };
  private itemCell(field: ItemField): ColDef<InvoiceItem> {
    return { cellRenderer: InvoiceItemCellComponent, cellRendererParams: {
      itemField: field,
      getSuggestions: () => this.itemRecords,
      isEditable: () => this.editing,
      onChange: (item: InvoiceItem) => this.onItemChange(item),
      onChoose: (item: InvoiceItem, option: InvoiceItem) => this.chooseItem(item, option),
    } };
  }
  readonly itemColumns: ColDef<InvoiceItem>[] = [
    { headerName: '', colId: 'remove', width: 40, minWidth: 40, maxWidth: 40, sortable: false, filter: false, resizable: false, suppressHeaderMenuButton: true, cellClass: 'invoice-item-remove-cell', cellRenderer: (p: ICellRendererParams<InvoiceItem>) => this.renderItemRemove(p) },
    { headerName: 'Description', field: 'itemDescription', minWidth: 220, flex: 2.4, ...this.itemCell('itemDescription') },
    { headerName: 'HSN code', field: 'hnsCode', minWidth: 85, flex: 1, ...this.itemCell('hnsCode') },
    { headerName: 'Qty', field: 'quantity', minWidth: 65, flex: .65, ...this.itemCell('quantity') },
    { headerName: 'Rate / Cost', field: 'rate', minWidth: 95, flex: 1, ...this.itemCell('rate') },
    { headerName: 'CGST %', field: 'cgst', minWidth: 70, flex: .8, ...this.itemCell('cgst') },
    { headerName: 'SGST %', field: 'sgst', minWidth: 70, flex: .8, ...this.itemCell('sgst') },
    { headerName: 'IGST %', field: 'igst', minWidth: 70, flex: .8, ...this.itemCell('igst') },
    { headerName: 'Total value', colId: 'total', minWidth: 120, flex: 1, cellClass: 'invoice-grid-amount', valueGetter: p => p.data ? this.amount(p.data) + this.taxAmount(p.data) : 0, valueFormatter: p => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(Number(p.value) || 0) },
  ];
  private listRequest = 0; private detailRequest = 0;

  ngOnInit() { this.updateFormColumns(); void this.loadFilters(); }
  @HostListener('window:resize') updateFormColumns() { this.formColumns = window.innerWidth <= 850 ? 1 : 3; }
  setInvoiceDate(value: Date | null) { this.invoiceDatePickerValue=value; if(this.summary) this.summary.invoiceDate=value ? this.normalizeDate(value.getTime()) : ''; }
  private syncInvoiceDatePicker() { this.invoiceDatePickerValue=this.summary?.invoiceDate ? new Date(`${this.summary.invoiceDate}T12:00:00`) : null; }
  private notify() { this.cd.markForCheck(); }
  private formatMoney(value: number | null | undefined) { return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(Number(value) || 0); }
  private formatGridDate(value: string | null | undefined) {
    if (!value) return '—';
    const date = new Date(/^\d{4}-\d{2}-\d{2}/.test(value) ? `${value.slice(0,10)}T12:00:00` : value);
    return Number.isNaN(date.getTime()) ? '—' : date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
  private renderInvoiceLink(params: ICellRendererParams<SaleSummary>) {
    const button = document.createElement('button');
    button.type = 'button'; button.className = 'invoice-grid-link';
    button.textContent = params.value || '—';
    if (params.data) button.addEventListener('click', () => this.zone.run(() => void this.showDetail(params.data!)));
    return button;
  }
  private renderComment(params: ICellRendererParams<SaleSummary>) {
    const comment = document.createElement('span');
    comment.className = 'invoice-grid-comment';
    comment.textContent = params.value || '—';
    comment.title = params.value || '';
    return comment;
  }
  private renderActions(params: ICellRendererParams<SaleSummary>) {
    const actions = document.createElement('div');
    actions.className = 'invoice-grid-actions';
    if (!params.data) return actions;
    const invoice = params.data;
    for (const [label, action] of [['Copy', () => void this.copyInvoices([invoice])], ['Edit', () => void this.showDetail(invoice, true)], ['PDF', () => void this.downloadPdf(invoice)]] as const) {
      const button = document.createElement('button');
      button.type = 'button'; button.className = 'table-action';
      button.textContent = label;
      button.setAttribute('aria-label', `${label} invoice ${invoice.invoiceId}`);
      button.addEventListener('click', event => { event.stopPropagation(); this.zone.run(action); });
      actions.appendChild(button);
    }
    return actions;
  }
  onGridReady(event: GridReadyEvent<SaleSummary>) { this.gridApi = event.api; }
  onItemGridReady(event: GridReadyEvent<InvoiceItem>) { this.itemGridApi = event.api; }
  private renderItemRemove(params: ICellRendererParams<InvoiceItem>) {
    const wrapper = document.createElement('span');
    if (!params.data || !this.editing) return wrapper;
    const button = document.createElement('button');
    button.type = 'button'; button.className = 'item-remove-button';
    button.textContent = '\u00d7';
    button.setAttribute('aria-label', `Remove item ${params.node.rowIndex == null ? '' : params.node.rowIndex + 1}`);
    button.title = 'Remove item';
    button.addEventListener('click', event => {
      event.stopPropagation();
      this.zone.run(() => this.removeItem(this.detail?.items.indexOf(params.data!) ?? -1));
    });
    wrapper.append(button);
    return wrapper;
  }
  onGridSelectionChanged(event: SelectionChangedEvent<SaleSummary>) {
    this.selected = new Set(event.api.getSelectedRows().map(invoice => this.key(invoice)));
    this.notify();
  }
  async copySelected() {
    const rows: SaleSummary[] = [];
    this.gridApi?.forEachNodeAfterFilterAndSort(node => { if (node.isSelected() && node.data) rows.push(node.data); });
    await this.copyInvoices(rows);
  }
  async copyInvoices(rows: SaleSummary[]) {
    if (!rows.length) return;
    const clean = (value: unknown) => String(value ?? '').replace(/[\t\r\n]+/g, ' ').trim();
    const lines = rows.map(invoice => [invoice.invoiceId, invoice.clientName, this.formatGridDate(invoice.invoiceDate), invoice.totalAmt, invoice.details].map(clean).join('\t'));
    const content = ['Invoice\tClient\tDate\tTotal\tComments', ...lines].join('\n');
    try {
      await navigator.clipboard.writeText(content);
      this.message = `${rows.length} invoice${rows.length === 1 ? '' : 's'} copied to clipboard.`;
      this.error = '';
    } catch {
      this.error = 'Could not access the clipboard. Please allow clipboard access and try again.';
    }
    this.notify();
  }
  displayClientOption = (option: Option | string | null): string => typeof option === 'string' ? option : option?.value ?? '';
  get matchingFilterClients(): Option[] {
    const query = (typeof this.clientFilterValue === 'string' ? this.clientFilterValue : '').trim().toLowerCase();
    return [...(query ? [] : [{ key: 0, value: 'All clients' }]), ...this.clients.filter(option => !query || option.value.toLowerCase().includes(query))].slice(0, 60);
  }
  onClientFilterInput(value: Option | string) {
    if (typeof value !== 'string') return;
    this.clientFilterValue = value;
    if (this.client && (value.trim() === '' || value !== this.clients.find(option => option.key === this.client)?.value)) { this.client = 0; void this.load(); }
    this.applyFilters();
  }
  selectFilterClient(option: Option) { this.clientFilterValue = option; if (this.client !== option.key) { this.client = option.key; void this.load(); } }
  displayEditorClient = (client: Client | string | null): string => typeof client === 'string' ? client : client?.clientName ?? '';
  get matchingEditorClients(): Client[] {
    const query = (typeof this.editorClientValue === 'string' ? this.editorClientValue : '').trim().toLowerCase();
    return this.clientRecords.filter(client => !query || client.clientName.toLowerCase().includes(query) || (client.GSTno || '').toLowerCase().includes(query)).slice(0, 60);
  }
  onEditorClientInput(value: Client | string) {
    if (typeof value !== 'string') return;
    this.editorClientValue = value;
    if (this.summary?.clientId && value !== this.summary.clientName) this.selectClient(0);
  }
  selectEditorClient(client: Client) { this.editorClientValue = client; this.selectClient(client.id); }
  private errorMessage(e: unknown, fallback: string) {
    if (e instanceof HttpErrorResponse) {
      if (e.status === 401 || e.status === 403 || e.url?.includes('/login')) return 'Please sign in again to continue.';
      if (e.status === 409) return 'This invoice number already exists. Enter a different number and save again.';
      if (e.error?.detail) return e.error.detail;
    }
    return fallback;
  }
  async loadFilters() {
    this.fyLoading = true; this.error = '';
    try {
      const response = await firstValueFrom(this.api.getYears());
      this.years = [...(response.data ?? [])].sort((a,b) => b.key-a.key);
      const current = this.api.currentFinancialYearKey();
      this.year = this.years.find(x => x.key === current)?.key ?? this.years[0]?.key ?? 0;
      if (!this.year) this.error = 'No financial years are configured.';
      else void this.load();
    } catch(e) { this.error = this.errorMessage(e,'Could not load financial years. Please retry.'); }
    finally { this.fyLoading = false; this.notify(); }
    if (this.year && this.route.snapshot.queryParamMap.get('create') === '1') {
      void this.router.navigate([], { relativeTo: this.route, queryParams: { create: null }, queryParamsHandling: 'merge', replaceUrl: true });
      void this.createInvoice();
    }
    try { this.clients = (await firstValueFrom(this.api.getClientOptions())).data ?? []; this.clientFilterValue = ''; }
    catch(e) { this.error = this.errorMessage(e,'Could not load the client filter.'); }
    this.notify();
  }
  async load() {
    if (!this.year) return;
    const request = ++this.listRequest;
    this.loading = true; this.error = ''; this.selected.clear(); this.gridApi?.deselectAll();
    try {
      const result = await firstValueFrom(this.api.getInvoices([this.year],this.client ? [this.client] : []));
      if (request === this.listRequest) { this.invoices = result.data ?? []; this.applyFilters(); }
    } catch(e) { if(request === this.listRequest) this.error = this.errorMessage(e,'Could not load invoices.'); }
    finally { if(request === this.listRequest) this.loading = false; this.notify(); }
  }
  applyFilters() {
    if (this.selected.size) { this.gridApi?.deselectAll(); this.selected.clear(); }
    const q = this.query.trim().toLowerCase();
    const clientQuery = this.client || typeof this.clientFilterValue !== 'string' ? '' : this.clientFilterValue.trim().toLowerCase();
    this.filteredInvoices = this.invoices.filter(x => (!q || x.invoiceId.toLowerCase().includes(q) || x.clientName.toLowerCase().includes(q) || (x.details||'').toLowerCase().includes(q)) && (!clientQuery || x.clientName.toLowerCase().includes(clientQuery)) && (!this.month || new Date(x.invoiceDate).getMonth()+1 === this.month))
      .sort((a,b) => (+new Date(b.invoiceDate)- +new Date(a.invoiceDate)) || b.invoiceId.localeCompare(a.invoiceId,undefined,{numeric:true}));
  }
  get filtered() { return this.filteredInvoices; }
  key(x: SaleSummary) { return JSON.stringify([x.fyYearId,x.invoiceId]); }
  private normalizeClient(client: Client): Client {
    const raw=client as Client & {gstno?:string;gstNo?:string};
    return {...client,GSTno:client.GSTno ?? raw.gstno ?? raw.gstNo ?? ''};
  }
  private async fetchClients() {
    this.clientRecords=((await firstValueFrom(this.api.getClients())).data ?? []).map(x=>this.normalizeClient(x))
      .sort((a,b)=>a.clientName.localeCompare(b.clientName));
  }
  private normalizeDate(value?: string | number | null): string {
    if (value == null || value === '') return '';
    if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}/.test(value)) return value.slice(0, 10);
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '' : [date.getFullYear(),String(date.getMonth()+1).padStart(2,'0'),String(date.getDate()).padStart(2,'0')].join('-');
  }
  private dateValue() {
    const now=new Date(); return [now.getFullYear(),String(now.getMonth()+1).padStart(2,'0'),String(now.getDate()).padStart(2,'0')].join('-');
  }
  async createInvoice() {
    if(!this.year) return;
    this.editorOpen=true; this.detailLoading=true; this.creating=true; this.editing=true; this.editorError='';
    this.detail=undefined; this.summary=undefined;
    const request=++this.detailRequest;
    try {
      const [number]=await Promise.all([firstValueFrom(this.api.nextInvoiceId(this.year)),this.fetchClients(),this.fetchItems()]);
      if(request!==this.detailRequest) return;
      this.summary={invoiceId:number.data,invoiceDate:this.dateValue(),clientName:'',clientId:0,fyYearId:this.year,
        fyYear:this.years.find(x=>x.key===this.year)?.value??'',totalAmt:0,payedAmt:0,payReceived:false,ragStatus:'',details:''};
      this.detail={invoiceId:number.data,fyear:this.year,invoiceDate:this.summary.invoiceDate,client:{} as Client,items:[],
        billAmount:0,taxAmount:0,grandTotal:0,deliveryDoc:'',deliveryDate:'',buyerDoc:'',buyerDocDate:'',destination:'',despatchedVia:''};
      this.editorClientValue = '';
      this.syncInvoiceDatePicker();
    } catch(e) { this.editorError=this.errorMessage(e,'Could not prepare the invoice. Close and try again.'); }
    finally { if(request===this.detailRequest) this.detailLoading=false; this.notify(); }
  }
  async showDetail(invoice: SaleSummary, edit=false) {
    const request=++this.detailRequest;
    this.editorOpen=true; this.detailLoading=true; this.creating=false; this.editing=edit; this.editorError=''; this.detail=undefined;
    this.summary={...invoice,invoiceDate:this.normalizeDate(invoice.invoiceDate)};
    try {
      const [result]=await Promise.all([firstValueFrom(this.api.getInvoiceDetail(invoice.invoiceId,invoice.fyYearId)),this.fetchClients(),this.fetchItems()]);
      if(request!==this.detailRequest) return;
      if(!result.data?.client) throw new Error('Missing invoice');
      this.summary.invoiceDate ||= this.normalizeDate(result.data.invoiceDate);
      this.detail={...result.data,client:this.normalizeClient(result.data.client),items:(result.data.items??[]).map(x=>({...x,tax:x.tax??0,cgst:x.cgst??0,sgst:x.sgst??0,igst:x.igst??0})),
        invoiceDate:this.summary.invoiceDate,
        deliveryDate:result.data.deliveryDate?.slice(0,10)??'',buyerDocDate:result.data.buyerDocDate?.slice(0,10)??''};
      this.editorClientValue = this.detail.client;
      this.syncInvoiceDatePicker();
    } catch(e) { this.editorError=this.errorMessage(e,'Could not load invoice details. Close and retry.'); }
    finally { if(request===this.detailRequest) this.detailLoading=false; this.notify(); }
  }
  private async fetchItems() { this.itemRecords=(await firstValueFrom(this.api.getItemOptions())).data ?? []; }
  chooseItem(item: InvoiceItem, option: InvoiceItem) {
    item.itemDescription=option.itemDescription; item.hnsCode=option.hnsCode; item.quantity=option.quantity??1; item.rate=option.rate??0;
    item.tax=option.tax??0; item.cgst=option.cgst??0; item.sgst=option.sgst??0; item.igst=option.igst??0;
    const node = this.itemGridApi?.getRenderedNodes().find(x => x.data === item);
    this.itemGridApi?.refreshCells({rowNodes:node ? [node] : undefined,force:true});
    this.notify();
  }
  onItemChange(item: InvoiceItem) {
    const node = this.itemGridApi?.getRenderedNodes().find(x => x.data === item);
    this.itemGridApi?.refreshCells({ rowNodes: node ? [node] : undefined, columns: ['total'], force: true });
    this.notify();
  }
  async changeFinancialYear(year: number) {
    if (!this.creating || !this.summary || !this.detail) return;
    const previousYear = this.summary.fyYearId;
    const previousInvoiceId = this.summary.invoiceId;
    const request = ++this.yearRequest;
    this.yearLoading = true;
    this.summary.fyYearId = year;
    this.summary.fyYear = this.years.find(x => x.key === year)?.value ?? String(year);
    this.detail.fyear = year;
    this.editorError = '';
    try {
      const result = await firstValueFrom(this.api.nextInvoiceId(year));
      if (request === this.yearRequest && this.summary?.fyYearId === year && this.summary.invoiceId === previousInvoiceId) {
        this.summary.invoiceId = result.data;
      }
    } catch(e) {
      if (request === this.yearRequest && this.summary) {
        this.summary.fyYearId = previousYear;
        this.summary.fyYear = this.years.find(x => x.key === previousYear)?.value ?? String(previousYear);
        this.detail!.fyear = previousYear;
        this.editorError = this.errorMessage(e, 'Could not load the next invoice ID for that financial year.');
      }
    } finally { if (request === this.yearRequest) this.yearLoading = false; this.notify(); }
  }
  selectClient(id: number) {
    const client=this.clientRecords.find(x=>x.id===id);
    if(!client) { if(this.summary) { this.summary.clientId=0; this.summary.clientName=''; } if(this.detail) this.detail.client={} as Client; return; }
    if(!this.summary || !this.detail) return;
    this.summary.clientId=client.id; this.summary.clientName=client.clientName; this.detail.client=client;
  }
  newItem(): InvoiceItem { return {id:0,itemDescription:'',hnsCode:'',quantity:1,rate:0,tax:0,cgst:0,sgst:0,igst:0,amount:0,taxAmount:0,totalAmount:0}; }
  addItem() { if(!this.detail) return; this.detail.items = [...this.detail.items, this.newItem()]; this.notify(); }
  removeItem(index: number) { if (this.detail && index >= 0) { this.detail.items = this.detail.items.filter((_, i) => i !== index); this.notify(); } }
  amount(item: InvoiceItem) { return (Number(item.quantity)||0)*(Number(item.rate)||0); }
  taxAmount(item: InvoiceItem) { return Math.round(this.amount(item)*((Number(item.tax)||0)+(Number(item.cgst)||0)+(Number(item.sgst)||0)+(Number(item.igst)||0)))/100; }
  get subtotal() { return this.detail?.items.reduce((sum,x)=>sum+this.amount(x),0)??0; }
  get taxTotal() { return Math.round((this.detail?.items.reduce((sum,x)=>sum+this.taxAmount(x),0)??0)*10)/10; }
  get grandTotal() { return Math.round(this.subtotal+this.taxTotal); }
  async save() {
    if(!this.summary || !this.detail || this.saving || this.yearLoading) return;
    if(!this.summary.invoiceDate) { this.editorError='Choose an invoice date.'; return; }
    if(!this.summary.clientId) { this.editorError='Select a client from the list.'; return; }
    if(!this.detail.items.length) { this.editorError='Add at least one line item.'; return; }
    this.saving=true; this.editorError='';
    try {
      const detail={...this.detail,invoiceDate:this.summary.invoiceDate,deliveryDate:this.detail.deliveryDate || null,buyerDocDate:this.detail.buyerDocDate || null} as unknown as InvoiceDetail;
      const response=await firstValueFrom(this.api.saveInvoice(this.summary,detail,this.creating));
      this.detail={...response.data,client:this.normalizeClient(response.data.client)};
      this.message=this.creating?'Invoice created.':'Invoice updated.';
      ++this.yearRequest;
      this.creating=false; this.editing=false; void this.load();
    } catch(e) { this.editorError=this.errorMessage(e,'Invoice could not be saved. Check the fields and try again.'); }
    finally { this.saving=false; this.notify(); }
  }
  closeDetail() { if(this.saving) return; ++this.detailRequest; ++this.yearRequest; this.yearLoading=false; this.editorOpen=false; this.detail=undefined; this.summary=undefined; }
  private downloadBlob(blob: Blob, name: string) {
    const url=URL.createObjectURL(blob); const link=document.createElement('a'); link.href=url; link.download=name; link.click();
    setTimeout(()=>URL.revokeObjectURL(url),10000);
  }
  async downloadPdf(invoice: SaleSummary) {
    this.downloading=true; this.error=''; this.editorError='';
    try { this.downloadBlob(await firstValueFrom(this.api.downloadInvoice(invoice.invoiceId,invoice.fyYearId)),
      'invoice-'+invoice.fyYearId+'-'+invoice.invoiceId.replace(/[^a-zA-Z0-9_-]/g,'_')+'.pdf'); }
    catch(e) { this.error=this.editorError=this.errorMessage(e,'PDF download failed. Please retry.'); }
    finally { this.downloading=false; this.notify(); }
  }
  async downloadZip() {
    const selected=this.invoices.filter(x=>this.selected.has(this.key(x)));
    if(!selected.length || selected.length>100) { this.error='Select between 1 and 100 invoices.'; return; }
    this.downloading=true; this.error='';
    try { this.downloadBlob(await firstValueFrom(this.api.downloadInvoices(selected)),'invoices-'+this.year+'.zip'); }
    catch(e) { this.error=this.errorMessage(e,'ZIP download failed. Please retry.'); }
    finally { this.downloading=false; this.notify(); }
  }
}
