import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Client, InvoiceDetail, InvoiceItem, Option, SaleSummary, SalesApiService } from '../../core/sales-api.service';

@Component({
  selector: 'app-invoices', standalone: true, imports: [CommonModule, FormsModule],
  templateUrl: './invoices.component.html'
})
export class InvoicesComponent implements OnInit {
  private readonly api = inject(SalesApiService);
  private readonly cd = inject(ChangeDetectorRef);
  years: Option[] = []; clients: Option[] = []; clientRecords: Client[] = []; itemRecords: InvoiceItem[] = []; invoices: SaleSummary[] = [];
  year = 0; client = 0; month = 0; query = ''; loading = false; fyLoading = true; downloading = false;
  error = ''; message = ''; selected = new Set<string>();
  editorOpen = false; detailLoading = false; saving = false; creating = false; editing = false;
  editorError = ''; clientSearch = ''; clientChosen = false; itemQuery = ''; detail?: InvoiceDetail; summary?: SaleSummary;
  private listRequest = 0; private detailRequest = 0;

  ngOnInit() { void this.loadFilters(); }
  private notify() { this.cd.markForCheck(); }
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
    try { this.clients = (await firstValueFrom(this.api.getClientOptions())).data ?? []; }
    catch(e) { this.error = this.errorMessage(e,'Could not load the client filter.'); }
    this.notify();
  }
  async load() {
    if (!this.year) return;
    const request = ++this.listRequest;
    this.loading = true; this.error = ''; this.selected.clear();
    try {
      const result = await firstValueFrom(this.api.getInvoices([this.year],this.client ? [this.client] : []));
      if (request === this.listRequest) this.invoices = result.data ?? [];
    } catch(e) { if(request === this.listRequest) this.error = this.errorMessage(e,'Could not load invoices.'); }
    finally { if(request === this.listRequest) this.loading = false; this.notify(); }
  }
  get filtered() {
    const q = this.query.trim().toLowerCase();
    return this.invoices.filter(x => (!q || x.invoiceId.toLowerCase().includes(q) || x.clientName.toLowerCase().includes(q) || (x.details||'').toLowerCase().includes(q)) && (!this.month || new Date(x.invoiceDate).getMonth()+1 === this.month))
      .sort((a,b) => (+new Date(b.invoiceDate)- +new Date(a.invoiceDate)) || b.invoiceId.localeCompare(a.invoiceId,undefined,{numeric:true}));
  }
  key(x: SaleSummary) { return JSON.stringify([x.fyYearId,x.invoiceId]); }
  toggle(x: SaleSummary) { const k=this.key(x); this.selected.has(k) ? this.selected.delete(k) : this.selected.add(k); }
  get allSelected() { return this.filtered.length > 0 && this.filtered.every(x => this.selected.has(this.key(x))); }
  toggleAll() { const checked=this.allSelected; this.filtered.forEach(x => checked ? this.selected.delete(this.key(x)) : this.selected.add(this.key(x))); }
  private normalizeClient(client: Client): Client {
    const raw=client as Client & {gstno?:string;gstNo?:string};
    return {...client,GSTno:client.GSTno ?? raw.gstno ?? raw.gstNo ?? ''};
  }
  private async fetchClients() {
    this.clientRecords=((await firstValueFrom(this.api.getClients())).data ?? []).map(x=>this.normalizeClient(x));
  }
  private dateValue(value?: string) {
    if(value) return value.slice(0,10);
    const now=new Date(); return [now.getFullYear(),String(now.getMonth()+1).padStart(2,'0'),String(now.getDate()).padStart(2,'0')].join('-');
  }
  async createInvoice() {
    if(!this.year) return;
    this.editorOpen=true; this.detailLoading=true; this.creating=true; this.editing=true; this.editorError='';
    this.detail=undefined; this.summary=undefined; this.clientChosen=false; this.clientSearch=''; this.itemQuery='';
    const request=++this.detailRequest;
    try {
      const [number]=await Promise.all([firstValueFrom(this.api.nextInvoiceId(this.year)),this.fetchClients(),this.fetchItems()]);
      if(request!==this.detailRequest) return;
      this.summary={invoiceId:number.data,invoiceDate:this.dateValue(),clientName:'',clientId:0,fyYearId:this.year,
        fyYear:this.years.find(x=>x.key===this.year)?.value??'',totalAmt:0,payedAmt:0,payReceived:false,ragStatus:'',details:''};
      this.detail={invoiceId:number.data,fyear:this.year,invoiceDate:this.summary.invoiceDate,client:{} as Client,items:[],
        billAmount:0,taxAmount:0,grandTotal:0,deliveryDoc:'',deliveryDate:'',buyerDoc:'',buyerDocDate:'',destination:'',despatchedVia:''};
    } catch(e) { this.editorError=this.errorMessage(e,'Could not prepare the invoice. Close and try again.'); }
    finally { if(request===this.detailRequest) this.detailLoading=false; this.notify(); }
  }
  async showDetail(invoice: SaleSummary, edit=false) {
    const request=++this.detailRequest;
    this.editorOpen=true; this.detailLoading=true; this.creating=false; this.editing=edit; this.editorError=''; this.detail=undefined; this.itemQuery='';
    this.summary={...invoice,invoiceDate:this.dateValue(invoice.invoiceDate)};
    try {
      const [result]=await Promise.all([firstValueFrom(this.api.getInvoiceDetail(invoice.invoiceId,invoice.fyYearId)),this.fetchClients(),this.fetchItems()]);
      if(request!==this.detailRequest) return;
      if(!result.data?.client) throw new Error('Missing invoice');
      this.detail={...result.data,client:this.normalizeClient(result.data.client),items:(result.data.items??[]).map(x=>({...x,tax:x.tax??0,cgst:x.cgst??0,sgst:x.sgst??0,igst:x.igst??0})),
        deliveryDate:result.data.deliveryDate?.slice(0,10)??'',buyerDocDate:result.data.buyerDocDate?.slice(0,10)??''};
      this.clientSearch=this.detail.client.clientName; this.clientChosen=true;
    } catch(e) { this.editorError=this.errorMessage(e,'Could not load invoice details. Close and retry.'); }
    finally { if(request===this.detailRequest) this.detailLoading=false; this.notify(); }
  }
  get clientMatches() {
    const q=this.clientSearch.trim().toLowerCase();
    return this.clientRecords.filter(x=>!q || x.clientName.toLowerCase().includes(q) || x.GSTno.toLowerCase().includes(q)).slice(0,15);
  }
  private async fetchItems() { this.itemRecords=(await firstValueFrom(this.api.getItemOptions())).data ?? []; }
  itemMatches(item: InvoiceItem) { const q=(item.itemDescription||'').trim().toLowerCase(); return q.length<1 ? [] : this.itemRecords.filter(x=>x.itemDescription.toLowerCase().includes(q)).slice(0,10); }
  chooseItem(item: InvoiceItem, option: InvoiceItem) { item.itemDescription=option.itemDescription; item.hnsCode=option.hnsCode; item.tax=option.tax??0; item.cgst=option.cgst??0; item.sgst=option.sgst??0; item.igst=option.igst??0; }
  get filteredItemCatalog() { const q=this.itemQuery.trim().toLowerCase(); return q ? this.itemRecords.filter(x=>x.itemDescription.toLowerCase().includes(q) || (x.hnsCode||'').toLowerCase().includes(q)) : []; }
  addCatalogItem(option: InvoiceItem) { if(!this.detail) return; const item={...this.newItem(),itemDescription:option.itemDescription,hnsCode:option.hnsCode,tax:option.tax??0,cgst:option.cgst??0,sgst:option.sgst??0,igst:option.igst??0}; this.detail.items.push(item); this.itemQuery=''; }
  clientTyped() { this.clientChosen=false; if(this.summary) this.summary.clientId=0; }
  chooseClient(client: Client) {
    if(!this.summary || !this.detail) return;
    this.summary.clientId=client.id; this.summary.clientName=client.clientName; this.detail.client=client;
    this.clientSearch=client.clientName; this.clientChosen=true;
  }
  newItem(): InvoiceItem { return {id:0,itemDescription:'',hnsCode:'',quantity:1,rate:0,tax:0,cgst:0,sgst:0,igst:0,amount:0,taxAmount:0,totalAmount:0}; }
  addItem() { this.detail?.items.push(this.newItem()); }
  removeItem(index: number) { this.detail?.items.splice(index,1); }
  amount(item: InvoiceItem) { return (Number(item.quantity)||0)*(Number(item.rate)||0); }
  taxAmount(item: InvoiceItem) { return Math.round(this.amount(item)*((Number(item.tax)||0)+(Number(item.cgst)||0)+(Number(item.sgst)||0)+(Number(item.igst)||0)))/100; }
  get subtotal() { return this.detail?.items.reduce((sum,x)=>sum+this.amount(x),0)??0; }
  get taxTotal() { return Math.round((this.detail?.items.reduce((sum,x)=>sum+this.taxAmount(x),0)??0)*10)/10; }
  get grandTotal() { return Math.round(this.subtotal+this.taxTotal); }
  async save() {
    if(!this.summary || !this.detail || this.saving) return;
    if(!this.clientChosen || !this.summary.clientId) { this.editorError='Choose a client from the search results.'; return; }
    if(!this.detail.items.length) { this.editorError='Add at least one line item.'; return; }
    this.saving=true; this.editorError='';
    try {
      const detail={...this.detail,deliveryDate:this.detail.deliveryDate || null,buyerDocDate:this.detail.buyerDocDate || null} as unknown as InvoiceDetail;
      const response=await firstValueFrom(this.api.saveInvoice(this.summary,detail,this.creating));
      this.detail={...response.data,client:this.normalizeClient(response.data.client)};
      this.message=this.creating?'Invoice created.':'Invoice updated.';
      this.creating=false; this.editing=false; void this.load();
    } catch(e) { this.editorError=this.errorMessage(e,'Invoice could not be saved. Check the fields and try again.'); }
    finally { this.saving=false; this.notify(); }
  }
  closeDetail() { if(this.saving) return; ++this.detailRequest; this.editorOpen=false; this.detail=undefined; this.summary=undefined; }
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
