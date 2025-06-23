import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { NominalCompositionService } from '../../core/services/nominal-composition.service';
import { NominalComposition } from '../../core/models/nominal-composition.model';
import { Run } from '../../core/models/run.model';

@Component({
  selector: 'app-pre-deployment-exploration',
  templateUrl: './pre-deployment-exploration.component.html',
  styleUrls: ['./pre-deployment-exploration.component.css']
})

export class PreDeploymentExplorationComponent implements OnInit {

  compositions: NominalComposition[] = [];
  selectedCompositionId: number | null = null;
  calculationCount: number = 1;
  runs: Run[] = [];

  activeTab: 'tab1' | 'tab2' = 'tab1';

  constructor(
    private compositionService: NominalCompositionService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.compositionService.getAll().subscribe({
      next: (data) => this.compositions = data,
      error: (err) => console.error('Failed to load compositions', err)
    });
  }

  increment(): void {
    if (this.calculationCount < 5) this.calculationCount++;
  }

  decrement(): void {
    if (this.calculationCount > 1) this.calculationCount--;
  }  

    // Called when switching to Tab 2 or selecting a new composition
  fetchRunsForSelectedComposition(): void {

    if (!this.selectedCompositionId) return;

    this.http.get<Run[]>(`http://localhost:8080/api/v1/crud/runs?nominalCompositionId=${this.selectedCompositionId}`)
      .subscribe({
        next: (data) => this.runs = data,
        error: (err) => {
          console.error('Failed to fetch runs', err);
          this.runs = [];
        }
      });

  }

  onCompositionSelected(nominalId: number): void {
    this.selectedCompositionId = nominalId;
    if (this.activeTab === 'tab2') {
      this.fetchRunsForSelectedComposition();
    }
  }

  activateTab2(): void {
    this.activeTab = 'tab2';
    if (this.selectedCompositionId) {
      this.fetchRunsForSelectedComposition();
    }
  }

}
