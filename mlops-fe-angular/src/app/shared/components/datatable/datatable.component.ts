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

  /*
  * NOTE: In Angular best practices, reusable components should be stateless when 
  *   possible, meaning they receive inputs and emit outputs; they don’t own business 
  *   logic like selecting/editing rows. Here the @Output of type EventEmitter is 
  *   used to establish communication with the parent component so that it can 
  *   handle any specific busines logic.
  */
  onCheckboxChange(keyValue: string): void {
    if (this.rowKeyColumn === keyValue) {
      this.rowSelected.emit(null); // deselect
    } else {
      this.rowSelected.emit(keyValue); // select new
    }
  }

}
