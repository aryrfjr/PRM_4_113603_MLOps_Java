/* 
 * Nominal Composition Manager that performs CRUD via HTTP to Spring backend.
 */

import { Component, OnInit } from '@angular/core';

import { NominalCompositionService } from 'src/app/core/services/nominal-composition.service';
import { NominalComposition } from 'src/app/core/models/nominal-composition.model';

@Component({
  selector: 'app-nominal-composition-manager',
  templateUrl: './nominal-composition-manager.component.html',
  styleUrls: ['./nominal-composition-manager.component.css']
})

export class NominalCompositionManagerComponent implements OnInit {

  // TODO: Add edit/delete icons to each row.
  // TODO: Style table with Angular Material or Bootstrap

  compositions: NominalComposition[] = [];
  loading = false;
  error: string | null = null;

  // Form State
  showForm = false;
  name = '';
  description = '';
  formError: string | null = null;
  formSuccess: string | null = null;

  constructor(private service: NominalCompositionService) {}

  ngOnInit(): void {
    this.fetchCompositions();
  }

  fetchCompositions(): void {

    this.loading = true;

    this.service.getAll().subscribe({
      next: (data) => {
        this.compositions = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load data';
        this.loading = false;
        console.error(err);
      }
    });

  }

  toggleForm(): void {

    this.showForm = !this.showForm;
    this.formError = null;
    this.formSuccess = null;

  }

  createComposition(): void {

    this.formError = null;
    this.formSuccess = null;

    const trimmedName = this.name.trim();

    if (!trimmedName) {
      this.formError = 'Name is required.';
      return;
    }

    const nameExists = this.compositions.some(
      c => c.name.toLowerCase() === trimmedName.toLowerCase()
    );
    
    if (nameExists) {
      this.formError = `Composition '${trimmedName}' already exists.`;
      return;
    }

    const payload = { name: trimmedName, description: this.description };

    this.service.create(payload).subscribe({
      next: () => {
        this.formSuccess = `Composition '${trimmedName}' created.`;
        this.name = '';
        this.description = '';
        this.fetchCompositions(); // Refresh list
      },
      error: (err) => {
        this.formError = 'Failed to create composition.';
        console.error(err);
      }
    });
    
  }

}
