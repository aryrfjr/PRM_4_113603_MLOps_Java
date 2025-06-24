import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { NominalCompositionService } from '../../core/services/nominal-composition.service';
import { NominalComposition } from '../../core/models/nominal-composition.model';
import { Run } from '../../core/models/run.model';
import { TableColumn } from '../../shared/components/datatable/datatable.component';

@Component({
  selector: 'app-pre-deployment-exploration',
  templateUrl: './pre-deployment-exploration.component.html',
  styleUrls: ['./pre-deployment-exploration.component.css']
})

export class PreDeploymentExplorationComponent implements OnInit {

  // Attributes related to the drop-down with Nominal Compositions
  compositions: NominalComposition[] = [];
  selectedComposition: string | null = null;
  selectedCompositionId: number | null = null;
  
  // Attributes related to the DataTable component for Runs
  runSelectedKey = "run_number"

  runsTableColumns: TableColumn[] = [
    { key: this.runSelectedKey, label: 'Run #', align: 'right' },
    { key: 'status', label: 'Status', align: 'center'  },
    { key: 'created_at', label: 'Created at', align: 'right', type: 'date', dateFormat: 'short' },
    { key: 'created_by', label: 'Created by' },
    { key: 'updated_at', label: 'Updated at', align: 'right', type: 'date', dateFormat: 'short' },
    { key: 'updated_by', label: 'Updated by' },
    { key: 'started_at', label: 'Started at', align: 'right', type: 'date', dateFormat: 'short' },
    { key: 'completed_at', label: 'Completed at', align: 'right', type: 'date', dateFormat: 'short' }
  ];

  runsTableData: Run[] = [];

  selectedRunNumber: string | null = null;

  // Attributes for the rest of UI components

  nRunsToSchedule: number = 1;

  activeTab: 'tab1' | 'tab2' = 'tab1';

  //
  // Methods
  //
  //////////////////////////////////////////////////////

  constructor(
    private compositionService: NominalCompositionService,
    private http: HttpClient
  ) {}

  /* 
   * NOTE: This method is a lifecycle hook defined by the OnInit interface. It is 
   *       automatically called by Angular once, just after the component is 
   *       created and initialized, but before it's displayed.
  */
  ngOnInit(): void {
    this.compositionService.getAll().subscribe({
      next: (data) => this.compositions = data,
      error: (err) => console.error('Failed to load compositions', err)
    });
  }

  // Increases the number of Runs to schedule
  increment(): void {
    if (this.nRunsToSchedule < 5) this.nRunsToSchedule++;
  }

  // Decreases the number of Runs to schedule
  decrement(): void {
    if (this.nRunsToSchedule > 1) this.nRunsToSchedule--;
  }  

  // Called when switching to Tab 2 or selecting a new composition
  fetchRunsForSelectedComposition(): void {

    if (!this.selectedCompositionId) return;

    this.http.get<Run[]>(`http://localhost:8080/api/v1/crud/runs?nominalCompositionId=${this.selectedCompositionId}`)
      .subscribe({
        next: (data) => this.runsTableData = data,
        error: (err) => {
          console.error('Failed to fetch runs', err);
          this.runsTableData = [];
        }
      });

  }

  onCompositionSelected(nominalId: number): void {

    // TODO: get the NC name

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

  selectRunRow(name: string | null): void {
    
    if (this.selectedRunNumber === name) {
      this.selectedRunNumber = null;
      return;
    }

    this.selectedRunNumber = name;

  }

}
