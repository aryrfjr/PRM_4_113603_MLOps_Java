// NOTE: This reusable component is declared in AppModule (app.module.ts).

import { Component, Input, Output, EventEmitter } from '@angular/core';

export interface TableColumn {
  key: string;
  label: string;
  align?: 'left' | 'center' | 'right';
}

@Component({
  selector: 'data-table', // defines the reusable <data-table> tag
  templateUrl: './datatable.component.html',
  styleUrls: ['./datatable.component.css']
})

export class DataTableComponent {

  // TODO: Style table with Angular Material or Bootstrap

  // Inputs that are passed in by the parent component
  @Input() columns: TableColumn[] = [];
  @Input() data: any[] = [];
  @Input() rowKeyColumn: string | null = null;
  @Input() selectedRowKeyValue: string | null = null;
  @Output() rowSelected = new EventEmitter<string | null>();

  onCheckboxChange(keyValue: string): void {
    if (this.rowKeyColumn === keyValue) {
      this.rowSelected.emit(null); // deselect
    } else {
      this.rowSelected.emit(keyValue); // select new
    }
  }

}
