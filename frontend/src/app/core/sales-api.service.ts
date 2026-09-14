import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface ApiResponse<T> { success: boolean; data: T; }
export interface Option { key: number; value: string; }
export interface SaleSummary {
  invoiceId: string; invoiceDate: string; clientName: string; clientId: number;
  fyYear: string; fyYearId: number; totalAmt: number; payedAmt: number;
  payReceived: boolean; ragStatus: string; details: string;
}
export interface Client { id: number; clientName: string; shortName: string; GSTno: string; contactPerson: string; emailAddress: string; address: string; telehoneNo: string; active: boolean; }
export interface InvoiceItem { id: number; itemDescription: string; hnsCode: string; rate: number; quantity: number; tax: number; cgst: number; sgst: number; igst: number; amount: number; taxAmount: number; totalAmount: number; }
export interface InvoiceDetail { invoiceId: string; fyear: number; invoiceDate: string; client: Client; items: InvoiceItem[]; billAmount: number; taxAmount: number; grandTotal: number; deliveryDoc: string; deliveryDate: string; buyerDoc: string; buyerDocDate: string; destination: string; despatchedVia: string; }
export interface Overview { sumBill: number; netRevenue: number; sumPartPymt: number; invoiceCount: number; }
export interface ClientSales { clientName: string[]; dataSets: { label: string; data: number[] }[]; }
export interface MonthlySales { dataSets: { label: string; data: number[] }[]; labels?: string[]; }

@Injectable({ providedIn: 'root' })
export class SalesApiService {
  private readonly http = inject(HttpClient);
  nextInvoiceId(year: number) { return this.http.get<ApiResponse<string>>(`/invoice/editor/next-id/${year}`); }
  saveInvoice(summary: SaleSummary, detail: InvoiceDetail, create: boolean) {
    return this.http.post<ApiResponse<InvoiceDetail>>(`/invoice/editor/${create ? 'create' : 'save'}`, { summary, detail });
  }
  downloadInvoice(invoiceId: string, fyear: number) {
    return this.http.post('/invoice/editor/pdf', { invoiceId, fyear }, { responseType: 'blob' });
  }
  downloadInvoices(invoices: SaleSummary[]) {
    return this.http.post('/invoice/editor/zip', invoices.map(x => ({invoiceId: x.invoiceId, fyear: x.fyYearId})), { responseType: 'blob' });
  }
  /** Indian financial year: April 1 through March 31; API keys are the starting year. */
  currentFinancialYearKey(today = new Date()): number {
    return today.getMonth() >= 3 ? today.getFullYear() : today.getFullYear() - 1;
  }
  getYears() { return this.http.get<ApiResponse<Option[]>>('/home/fy'); }
  getClientOptions() { return this.http.get<ApiResponse<Option[]>>('/home/clients'); }
  getClients() { return this.http.get<ApiResponse<Client[]>>('/client/list'); }
  saveClient(client: Client, create: boolean) { return this.http.post<ApiResponse<Client>>(`/client/${create ? 'create' : 'update'}`, client); }
  getItemOptions() { return this.http.get<ApiResponse<InvoiceItem[]>>('/invoice/editor/items'); }
  getInvoices(years: number[], clientids: number[] = []) {
    return this.http.post<ApiResponse<SaleSummary[]>>('/home/summary', { years, clientids });
  }
  getOverview(year: number, clientids: number[] = []) {
    return this.http.post<ApiResponse<Overview>>('/home/overview', { years: [year], clientids });
  }
  getClientSales(year: number) { return this.http.get<ApiResponse<ClientSales>>(`/graph/sales/client/${year}`); }
  getMonthlySales(year: number) { return this.http.get<ApiResponse<MonthlySales>>(`/graph/sales/month/${year}`); }
  getInvoiceDetail(invoiceId: string, fyear: number) {
    return this.http.post<ApiResponse<InvoiceDetail>>('/invoice/item/detail', { invoiceId, fyear });
  }
}
