// NOTE: This reusable component is declared in AppModule (app.module.ts).

import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';

import { finalize } from 'rxjs/operators';

import { SubRun } from '../../../../core/models/sub-run.model';
import { TableColumn } from '../../../../shared/components/datatable/datatable.component';
import { SubRunService } from '../../../../core/services/sub-run.service';

@Component({
  selector: 'sub-runs-data-table', // defines the reusable <sub-runs-data-table> tag
  templateUrl: './sub-runs-datatable.component.html'
})

export class SubRunsDataTableComponent implements OnChanges {

  @Input() selectedRunId: number | null = null;
  @Input() selectedRunNumber: number | null = null;

  //
  // Support to a kind of ViewModel from MVVM (Model-View-ViewModel) approach
  //
  //////////////////////////////////////////////////////

  get uiState() {

    return {
      displaySimulationArtifactsTable: this.selectedSubRunId != null
    };

  }

  // To avoid duplicate calls to method fetchSubRunsForSelectedRun()
  private lastFetchedRunId: number | null = null;

    // Messages and flags related to data operations
  serviceRequestOn = false;
  serviceRequestErrorMessage: string | null = null;

  // Data structures
  subRunsTableData: SubRun[] = [];
  subRunSelectedKey = "id"
  selectedSubRunId: number | null = null;
  selectedSubRunNumber: number | null = null;

  subRunsTableColumns: TableColumn[] = [
    { key: 'sub_run_number', label: 'sub-Run #', align: 'right' },
    { key: 'status', label: 'Status', align: 'center'  },
    { key: 'created_at', label: 'Created at', align: 'right', type: 'date', dateFormat: 'short' },
    { key: 'created_by', label: 'Created by' },
    { key: 'updated_at', label: 'Updated at', align: 'right', type: 'date', dateFormat: 'short' },
    { key: 'updated_by', label: 'Updated by' },
    { key: 'started_at', label: 'Started at', align: 'right', type: 'date', dateFormat: 'short' },
    { key: 'completed_at', label: 'Completed at', align: 'right', type: 'date', dateFormat: 'short' }
  ];

  constructor(
    private subRunService: SubRunService,
  ) {}

  // From interface OnChanges
  ngOnChanges(changes: SimpleChanges): void {

    // If the inputs selectedRunId and selectedRunName changed in the parent component
    if (
      changes['selectedRunId'] &&
      this.selectedRunId &&
      this.selectedRunId !== this.lastFetchedRunId
    ) {
      this.fetchSubRunsForSelectedRun();
      this.lastFetchedRunId = this.selectedRunId;
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

  private fetchSubRunsForSelectedRun(): void {

    if (!this.selectedRunId) return;

    this.startedServiceRequest();

    this.subRunService.getAll(this.selectedRunId).pipe(
      finalize(() => {
        this.finalizedServiceRequest();
      })
    ).subscribe({
        next: (data) => {
          this.subRunsTableData = data
        },
        error: (err) => {
          console.error('Failed to fetch SubRuns', err);
          this.serviceRequestErrorMessage = `Failed to fetch SubRuns. Error: ${err?.error?.message}`;
          this.cleanSubRunsInfo();
        }
      });

  }

  onSubRunsTableRowSelected(subRunId: number | string | null): void {

    // When user deselects
    if (this.selectedSubRunId === subRunId) {
      this.selectedSubRunId = null;
      this.selectedSubRunNumber = null;
    } else {
      // When user selects
      this.selectedSubRunId = subRunId as number;
      this.selectedSubRunNumber = this.subRunsTableData.find(subRun => subRun.id === subRunId)?.sub_run_number ?? null;
    }

  }

  cleanSubRunsInfo() {

      this.subRunsTableData = [];
      this.selectedSubRunId = null;
      this.selectedSubRunNumber = null;

  }

}
