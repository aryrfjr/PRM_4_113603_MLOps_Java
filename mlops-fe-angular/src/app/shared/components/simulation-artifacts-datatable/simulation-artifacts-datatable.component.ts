// NOTE: This reusable component is declared in AppModule (app.module.ts).

import { Component, Input } from '@angular/core';

import { finalize } from 'rxjs/operators';

import { SimulationArtifact } from '../../../core/models/simulation-artifact.model';
import { TableColumn } from '../../../shared/components/datatable/datatable.component';
import { SimulationArtifactService } from '../../../core/services/simulation-artifact.service';

@Component({
  selector: 'simulation-artifacts-data-table', // defines the reusable <simulation-artifacts-data-table> tag
  templateUrl: './simulation-artifacts-datatable.component.html'
})

export class SimulationArtifactsDataTableComponent {

  @Input() selectedSubRunId: number | null = null;
  @Input() selectedSubRunNumber: number | null = null;

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

  cleanMessages(): void {
    this.serviceRequestErrorMessage = null;
  }

  startedServiceRequest(): void {
    this.serviceRequestOn = true;
    this.cleanMessages();
  }

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
