/* 
 * Nominal Composition Manager that performs CRUD via HTTP to Spring backend.
 */

import { Component, OnInit } from '@angular/core';

import { NominalCompositionService } from 'src/app/core/services/nominal-composition.service';
import { NominalComposition } from 'src/app/core/models/nominal-composition.model';
import { TableColumn } from '../../shared/components/datatable/datatable.component';

@Component({
  selector: 'app-nominal-composition-manager',
  templateUrl: './nominal-composition-manager.component.html',
  styleUrls: ['./nominal-composition-manager.component.css']
})

export class NominalCompositionManagerComponent implements OnInit {

  // Attributes for DataTable component
  ncSelectedKey = "name"

  ncTableColumns: TableColumn[] = [
    { key: this.ncSelectedKey, label: 'Name' },
    { key: 'description', label: 'Description' },
    { key: 'created_at', label: 'Created at', align: 'right', type: 'date', dateFormat: 'short' },
    { key: 'updated_at', label: 'Updated at', align: 'right', type: 'date', dateFormat: 'short' },
    { key: 'created_by', label: 'Created by' },
    { key: 'updated_by', label: 'Updated by' }
  ];

  ncTableData: NominalComposition[] = [];
  loading = false;
  error: string | null = null;

  // Forms States
  showCreateForm = false;
  showEditForm = false;

  // Attributes for '+ Create'/'= Edit' actions
  selectedNCName: string | null = null;
  formName = '';
  formDescription = '';
  formMode: 'create' | 'edit' | null = null; // determines which mode the form is in
  formError: string | null = null;
  formSuccess: string | null = null;


  constructor(private service: NominalCompositionService) {}

  //
  // Methods to control UI components
  //
  //////////////////////////////////////////////////////////

  ngOnInit(): void {
    this.fetchCompositions();
  }

  toggleCreateMode(): void {

    this.clearForm();
    this.formMode = this.formMode === 'create' ? null : 'create';
    this.formName = '';
    this.formDescription = '';

  }

  selectRow(name: string | null): void {
    
    if (this.selectedNCName === name) {
      this.selectedNCName = null;
      this.formMode = null;
      return;
    }

    this.selectedNCName = name;
    const current = this.ncTableData.find(c => c.name === name);
    this.formName = current?.name ?? '';
    this.formDescription = current?.description ?? '';
    this.formMode = 'edit';
    this.formSuccess = null;
    this.formError = null;

  }

  clearForm(): void {

    this.formError = null;
    this.formSuccess = null;
    this.selectedNCName = null;
    this.formName = '';
    this.formDescription = '';

  }

  //
  // CRUD methods
  //
  //////////////////////////////////////////////////////////


  submitForm(): void {

    this.formError = null;
    this.formSuccess = null;

    const trimmedName = this.formName.trim();

    if (!trimmedName) {
      this.formError = 'Name is required.';
      return;
    }

    if (this.formMode === 'create') {
      
      const nameExists = this.ncTableData.some(
        c => c.name.toLowerCase() === trimmedName.toLowerCase()
      );

      if (nameExists) {
        this.formError = `Composition '${trimmedName}' already exists.`;
        return;
      }

      const payload = { name: trimmedName, description: this.formDescription };

      this.service.create(payload).subscribe({
        next: () => {
          this.formSuccess = `'${trimmedName}' created.`;
          this.clearForm();
          this.fetchCompositions();
        },
        error: (err) => {
          this.formError = 'Failed to create.';
          console.error(err);
        }
      });

    } else if (this.formMode === 'edit' && this.selectedNCName) {

      const payload = { description: this.formDescription };

      this.service.update(this.selectedNCName, payload).subscribe({
        next: () => {
          this.formSuccess = `'${this.selectedNCName}' updated.`;
          this.fetchCompositions();
        },
        error: (err) => {
          this.formError = 'Failed to update.';
          console.error(err);
        }
      });

    }

  }

  fetchCompositions(): void {

    this.loading = true;

    this.service.getAll().subscribe({
      next: (data) => {
        this.ncTableData = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load data';
        this.loading = false;
        console.error(err);
      }
    });

  }

  deleteSelected(): void {

    if (!this.selectedNCName) return;

    if (confirm(`Are you sure you want to delete '${this.selectedNCName}'?`)) {
      this.service.delete(this.selectedNCName).subscribe({
        next: () => {
          this.selectedNCName = null;
          this.formMode = null;
          this.fetchCompositions();
        },
        error: (err) => {
          this.error = 'Failed to delete.';
          console.error(err);
        }
      });
    }

  }

}
