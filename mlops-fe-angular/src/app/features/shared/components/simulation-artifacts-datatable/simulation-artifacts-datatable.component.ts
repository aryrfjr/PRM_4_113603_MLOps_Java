// NOTE: This reusable component is declared in AppModule (app.module.ts).

import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';

import { finalize } from 'rxjs/operators';

import { SimulationArtifact } from '../../../../core/models/simulation-artifact.model';
import { TableColumn } from '../../../../shared/components/datatable/datatable.component';
import { SimulationArtifactService } from '../../../../core/services/simulation-artifact.service';

@Component({
  selector: 'simulation-artifacts-data-table', // defines the reusable <simulation-artifacts-data-table> tag
  templateUrl: './simulation-artifacts-datatable.component.html'
})

/*
* NOTE: Since visibility of this component depends on changes in the input 
*   selectedSubRunId, the method fetchSimulationArtifactsForSelectedSubRun 
*   can be triggered in a reactive way.
* 
* NOTE: Why not use ngOnInit from interface OnInit? Because it runs 
*   only once, when the component is first created. If the parent 
*   component uses *ngIf to toggle visibility, re-shown instances don’t 
*   trigger ngOnInit() again; but ngOnChanges() always reacts to new input.
*/
export class SimulationArtifactsDataTableComponent implements OnChanges {

  @Input() selectedSubRunId: number | null = null;
  @Input() selectedSubRunNumber: number | null = null;

  // To avoid duplicate calls to method fetchSimulationArtifactsForSelectedSubRun()
  private lastFetchedSubRunId: number | null = null;

    // Messages and flags related to data operations
  serviceRequestOn = false;
  serviceRequestErrorMessage: string | null = null;

  // Data structures
  simulationArtifactsTableData: SimulationArtifact[] = [];

  simulationArtifactsTableColumns: TableColumn[] = [
    { key: 'artifact_type', label: 'Artifact Type', align: 'center'  },
    { key: 'file_path', label: 'File Path' },
    { key: 'file_size', label: 'File Size' },
    { key: 'checksum', label: 'Checksum' }
  ];

  constructor(
    private simulationArtifactService: SimulationArtifactService,
  ) {}

  // From interface OnChanges
  ngOnChanges(changes: SimpleChanges): void {
    
    if (
      changes['selectedSubRunId'] &&
      this.selectedSubRunId &&
      this.selectedSubRunId !== this.lastFetchedSubRunId
    ) {
      this.fetchSimulationArtifactsForSelectedSubRun();
      this.lastFetchedSubRunId = this.selectedSubRunId;
    }
    
  }

  // TODO: Go to an interface? We also have this method in parent component.
  cleanMessages(): void {
    this.serviceRequestErrorMessage = null;
  }

  // TODO: Go to an interface? We also have this method in parent component.
  startedServiceRequest(): void {
    this.serviceRequestOn = true;
    this.cleanMessages();
  }

  // TODO: Go to an interface? We also have this method in parent component.
  finalizedServiceRequest(): void {
    this.serviceRequestOn = false;
  }

  private fetchSimulationArtifactsForSelectedSubRun(): void {

    if (!this.selectedSubRunId) return;

    this.startedServiceRequest();

    this.simulationArtifactService.getAll(this.selectedSubRunId).pipe(
      finalize(() => {
        this.finalizedServiceRequest();
      })
    ).subscribe({
        next: (data) => {
          this.simulationArtifactsTableData = data
        },
        error: (err) => {
          console.error('Failed to fetch Simulation Artifacts', err);
          this.serviceRequestErrorMessage = `Failed to fetch Simulation Artifacts. Error: ${err?.error?.message}`;
          this.cleanSimulationArtifactsInfo();
        }
      });

  }

    private cleanSimulationArtifactsInfo() {
      this.simulationArtifactsTableData = [];
  }

}
