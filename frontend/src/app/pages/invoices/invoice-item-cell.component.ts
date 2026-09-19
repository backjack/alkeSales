import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
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
  imports: [CommonModule, FormsModule, MatAutocompleteModule],
  template: `
    <ng-container *ngIf="params.data as item">
      <textarea *ngIf="field === 'itemDescription'; else singleInput" class="grid-item-input grid-item-description" rows="2"
        [attr.aria-label]="label" [title]="item.itemDescription" [matAutocomplete]="suggestions"
        [ngModel]="item.itemDescription" (ngModelChange)="onDescriptionChange($event)" [disabled]="!params.isEditable()"></textarea>
      <ng-template #singleInput><input class="grid-item-input" type="text" [attr.aria-label]="label"
        [attr.inputmode]="numeric ? 'decimal' : 'text'" [ngModel]="item[field]"
        (input)="update($any($event.target).value)" [disabled]="!params.isEditable()"></ng-template>
      <mat-autocomplete #suggestions="matAutocomplete" class="invoice-item-autocomplete" [displayWith]="displayItemOption" (optionSelected)="choose($event.option.value)">
        <mat-option *ngFor="let option of matches" [value]="option">
          <span class="invoice-item-option">
            <span class="invoice-item-option-description">{{option.itemDescription}}</span>
            <span class="invoice-item-option-meta">HSN {{option.hnsCode || '—'}} · Rate {{option.rate | currency:'INR':'symbol':'1.2-2'}}</span>
          </span>
        </mat-option>
      </mat-autocomplete>
    </ng-container>
  `,
})
export class InvoiceItemCellComponent implements ICellRendererAngularComp {
  private readonly cd = inject(ChangeDetectorRef);
  params!: ItemCellParams;
  agInit(params: ItemCellParams): void { this.params = params; }
  refresh(params: ItemCellParams): boolean { this.params = params; this.cd.detectChanges(); return true; }
  get field(): ItemField { return this.params.itemField; }
  get numeric(): boolean { return !['itemDescription', 'hnsCode'].includes(this.field); }
  displayItemOption = (option: InvoiceItem | string | null): string => typeof option === 'string' ? option : option?.itemDescription ?? '';
  get label(): string { return ({ itemDescription: 'Description', hnsCode: 'HSN code', quantity: 'Quantity', rate: 'Rate', cgst: 'CGST percent', sgst: 'SGST percent', igst: 'IGST percent' } as Record<ItemField, string>)[this.field]; }
  get matches(): InvoiceItem[] {
    const query = (this.params.data?.itemDescription || '').trim().toLowerCase();
    return this.params.getSuggestions().filter(x => !query || x.itemDescription.toLowerCase().includes(query) || (x.hnsCode || '').toLowerCase().includes(query)).slice(0, 8);
  }
  onDescriptionChange(value: string | InvoiceItem): void {
    if (typeof value === 'string') this.update(value);
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
