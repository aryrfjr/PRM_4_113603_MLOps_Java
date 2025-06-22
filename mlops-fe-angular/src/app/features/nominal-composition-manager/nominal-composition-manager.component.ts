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

  compositions: NominalComposition[] = [];
  loading = false;
  error: string | null = null;

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
}
