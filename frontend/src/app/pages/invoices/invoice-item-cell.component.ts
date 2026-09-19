import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';
import { InvoiceItem } from '../../core/sales-api.service';

export type ItemField = 'itemDescription' | 'hnsCode' | 'quantity' | 'rate' | 'cgst' | 'sgst' | 'igst';
export interface ItemCellParams extends ICellRendererParams<InvoiceItem> {
  itemField: ItemField;
  getSuggestions: () => InvoiceItem[];
  isEditable: () => boolean;
  onChange: (item: InvoiceItem) => void;
  onChoose: (item: InvoiceItem, option: InvoiceItem) => void;
}

@Component({
  selector: 'app-invoice-item-cell', standalone: true,
  imports: [CommonModule, FormsModule, MatAutocompleteModule, MatFormFieldModule, MatInputModule],
  template: `
    <mat-form-field appearance="outline" subscriptSizing="dynamic" class="grid-item-field" *ngIf="params.data as item">
      <textarea *ngIf="field === 'itemDescription'; else singleInput" matInput rows="2"
        [attr.aria-label]="label" [title]="item.itemDescription" [matAutocomplete]="suggestions"
        [ngModel]="item.itemDescription" (ngModelChange)="update($event)" [disabled]="!params.isEditable()"></textarea>
      <ng-template #singleInput><input matInput [type]="numeric ? 'number' : 'text'" [attr.aria-label]="label"
        [min]="numeric ? 0 : null" [ngModel]="item[field]" (ngModelChange)="update($event)" [disabled]="!params.isEditable()"></ng-template>
      <mat-autocomplete #suggestions="matAutocomplete">
        <mat-option *ngFor="let option of matches" [value]="option.itemDescription"
          (onSelectionChange)="$event.isUserInput && choose(option)">{{option.itemDescription}} · HSN {{option.hnsCode || '—'}}</mat-option>
      </mat-autocomplete>
    </mat-form-field>
  `,
})
export class InvoiceItemCellComponent implements ICellRendererAngularComp {
  params!: ItemCellParams;
  agInit(params: ItemCellParams): void { this.params = params; }
  refresh(params: ItemCellParams): boolean { this.params = params; return true; }
  get field(): ItemField { return this.params.itemField; }
  get numeric(): boolean { return !['itemDescription', 'hnsCode'].includes(this.field); }
  get label(): string { return ({ itemDescription: 'Description', hnsCode: 'HSN code', quantity: 'Quantity', rate: 'Rate', cgst: 'CGST percent', sgst: 'SGST percent', igst: 'IGST percent' } as Record<ItemField, string>)[this.field]; }
  get matches(): InvoiceItem[] {
    const query = (this.params.data?.itemDescription || '').trim().toLowerCase();
    return this.params.getSuggestions().filter(x => !query || x.itemDescription.toLowerCase().includes(query) || (x.hnsCode || '').toLowerCase().includes(query)).slice(0, 8);
  }
  update(value: string | number): void {
    const item = this.params.data;
    if (!item) return;
    if (this.numeric) {
      (item as unknown as Record<string, number>)[this.field] = Number(value) || 0;
    } else {
      (item as unknown as Record<string, string>)[this.field] = String(value ?? '');
    }
    this.params.onChange(item);
  }
  choose(option: InvoiceItem): void {
    if (this.params.data) this.params.onChoose(this.params.data, option);
  }
}
