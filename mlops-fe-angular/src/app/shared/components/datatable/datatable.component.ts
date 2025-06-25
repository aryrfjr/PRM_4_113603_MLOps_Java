// NOTE: This reusable component is declared in AppModule (app.module.ts).

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { DatePipe } from '@angular/common';

export interface TableColumn {
  key: string;
  label: string;
  align?: 'left' | 'center' | 'right';
  type?: 'string' | 'date' | 'number';  // TODO: more?
  dateFormat?: string;  // e.g. 'short', 'medium', or custom Angular date formats
}

@Component({
  selector: 'data-table', // defines the reusable <data-table> tag
  templateUrl: './datatable.component.html',
  styleUrls: ['./datatable.component.css'],
  providers: [DatePipe]
})

export class DataTableComponent {

  // TODO: Style table with Angular Material or Bootstrap

  // Inputs that are passed in by the parent component
  @Input() columns: TableColumn[] = [];
  @Input() data: any[] = [];
  @Input() rowKeyColumn: string | null = null; // The field used to identify rows
  @Input() selectedRowKeyValue: number | string | null = null; // Currently selected value
  @Output() rowSelected = new EventEmitter<number | string | null>();

  constructor(private datePipe: DatePipe) {}

  /*
  * NOTE: In Angular best practices, reusable components should be stateless when 
  *   possible, meaning they receive inputs and emit outputs; they don’t own business 
  *   logic like selecting/editing rows. Here the @Output of type EventEmitter is 
  *   used to establish communication with the parent component so that it can 
  *   handle any specific busines logic.
  */
  onCheckboxChange(keyValue: number | string): void {
    if (this.rowKeyColumn === keyValue) {
      this.rowSelected.emit(null); // deselect
    } else {
      this.rowSelected.emit(keyValue); // select new
    }
  }

  formatCell(row: any, col: TableColumn): string {
    
    if (col.type === 'date') {
      return this.datePipe.transform(row[col.key], col.dateFormat || 'medium') || '';
    }

    // fallback to default string display
    return row[col.key];
    
  }

}
