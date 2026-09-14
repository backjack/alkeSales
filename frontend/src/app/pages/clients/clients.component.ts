import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Client, SalesApiService } from '../../core/sales-api.service';

@Component({selector:'app-clients',standalone:true,imports:[CommonModule,FormsModule],template:`
<section class="page"><div class="page-heading"><div><p class="eyebrow">Customers</p><h1>Clients</h1><p>Manage customer details used for invoicing.</p></div><button class="primary" (click)="openCreate()">+ Add client</button></div>
<div class="filter-bar"><label class="search">Search clients<input [(ngModel)]="query" placeholder="Name, contact, GST or email"></label></div><p class="error" *ngIf="error">{{error}}</p>
<div class="client-grid"><article class="client-card" *ngFor="let x of filtered"><div class="client-card-head"><span class="company-avatar">{{initials(x.clientName)}}</span><span class="status paid">{{x.active ? 'Active' : 'Inactive'}}</span></div><h2>{{x.clientName}}</h2><p>{{x.contactPerson || 'No contact person'}}</p><dl><div><dt>GST number</dt><dd>{{x.GSTno || '—'}}</dd></div><div><dt>Email</dt><dd>{{x.emailAddress || '—'}}</dd></div></dl><button class="table-action" (click)="openEdit(x)">Edit client</button></article></div><p class="empty" *ngIf="!filtered.length">No clients match your search.</p></section>
<div class="detail-backdrop" *ngIf="editorOpen" (click)="close()"><aside class="invoice-detail client-editor" (click)="$event.stopPropagation()"><button class="detail-close" (click)="close()">×</button><p class="eyebrow">{{creating?'New client':'Edit client'}}</p><h1>{{creating?'Add client':'Update client'}}</h1><div class="editor-grid"><label>Client name<input [(ngModel)]="draft.clientName" required></label><label>Short name<input [(ngModel)]="draft.shortName"></label><label>GST number<input [(ngModel)]="draft.GSTno"></label><label>Contact person<input [(ngModel)]="draft.contactPerson"></label><label>Email<input type="email" [(ngModel)]="draft.emailAddress"></label><label>Telephone<input [(ngModel)]="draft.telehoneNo"></label><label class="wide">Address<textarea [(ngModel)]="draft.address"></textarea></label></div><p class="error" *ngIf="editorError">{{editorError}}</p><div class="detail-actions"><button class="secondary" (click)="close()">Cancel</button><button class="primary" (click)="save()" [disabled]="saving">{{saving?'Saving…':'Save client'}}</button></div></aside></div>`})
export class ClientsComponent implements OnInit {
 private readonly api=inject(SalesApiService); private readonly cd=inject(ChangeDetectorRef);
 clients:Client[]=[]; query=''; error=''; editorError=''; editorOpen=false; creating=false; saving=false; draft:Client=this.blank();
 ngOnInit(){this.load();}
 async load(){try{this.clients=(await firstValueFrom(this.api.getClients())).data??[];}catch(e){this.error='Could not load clients.';}this.cd.markForCheck();}
 blank():Client{return {id:0,clientName:'',shortName:'',GSTno:'',contactPerson:'',emailAddress:'',address:'',telehoneNo:'',active:true};}
 openCreate(){this.creating=true;this.draft=this.blank();this.editorError='';this.editorOpen=true;}
 openEdit(client:Client){this.creating=false;this.draft={...client};this.editorError='';this.editorOpen=true;}
 close(){if(!this.saving)this.editorOpen=false;}
 async save(){if(!this.draft.clientName.trim()){this.editorError='Client name is required.';return;}this.saving=true;this.editorError='';try{const response=await firstValueFrom(this.api.saveClient(this.draft,this.creating));this.clients=this.creating?[...this.clients,response.data]:this.clients.map(x=>x.id===response.data.id?response.data:x);this.editorOpen=false;}catch(e){this.editorError='Could not save client. Please check the fields and retry.';}finally{this.saving=false;this.cd.markForCheck();}}
 get filtered(){const q=this.query.toLowerCase();return this.clients.filter(x=>!q||[x.clientName,x.contactPerson,x.GSTno,x.emailAddress].some(v=>v?.toLowerCase().includes(q)));}
 initials(name:string){return (name||'?').split(/\\s+/).slice(0,2).map(x=>x[0]).join('').toUpperCase();}
}
