import { Component, OnInit } from '@angular/core';

import { finalize } from 'rxjs/operators';

import { NominalCompositionService } from '../../core/services/nominal-composition.service';
import { RunService } from '../../core/services/run.service';
import { SubRunService } from '../../core/services/sub-run.service';
import { SimulationArtifactService } from '../../core/services/simulation-artifact.service';
import { DataOpsService } from '../../core/services/data-ops.service';
import { NominalComposition } from '../../core/models/nominal-composition.model';
import { Run } from '../../core/models/run.model';
import { SubRun } from '../../core/models/sub-run.model';
import { SimulationArtifact } from '../../core/models/simulation-artifact.model';
import { TableColumn } from '../../shared/components/datatable/datatable.component';

@Component({
  selector: 'app-pre-deployment-exploration',
  templateUrl: './pre-deployment-exploration.component.html',
  styleUrls: ['./pre-deployment-exploration.component.css']
})

export class PreDeploymentExplorationComponent implements OnInit {

  //
  // Support to a kind of ViewModel from MVVM (Model-View-ViewModel) approach
  //
  //////////////////////////////////////////////////////

  get uiState() {

    return {
      displayLoadingMessage: this.serviceRequestOn,
      displayScheduleForm: !this.serviceRequestOn && this.selectedNominalCompositionId != null,
      displayRunsTable: this.runsTableData.length > 0,
      displaySubRunsTable: this.subRunsTableData.length > 0,
      displaySimulationArtifactsTable: this.simulationArtifactsTableData.length > 0,
      suggestNominalCompositionSelection: !this.serviceRequestOn && this.selectedNominalCompositionId === null,
      isTabScheduleActive: this.activeTab === 'tab1',
      isTabViewActive: this.activeTab === 'tab2',
      displayAlertMessage: this.serviceRequestErrorMessage != null || this.scheduleSuccessMessage != null,
      alertMessageType: this.serviceRequestErrorMessage != null ? "error" : this.scheduleSuccessMessage != null ? "success" : null,
      alertMessage: this.serviceRequestErrorMessage ?? this.scheduleSuccessMessage ?? null
    };

  }

  // Messages and flags related to data operations
  serviceRequestOn = false;
  serviceRequestErrorMessage: string | null = null;
  scheduleSuccessMessage: string | null = null;

  //
  // Attributes related to the drop-down with Nominal Compositions
  //
  //////////////////////////////////////////////////////

  allNominalCompositions: NominalComposition[] = [];
  selectedNominalCompositionId: number | null = null; // NOTE: Explicitly set to null; an intentional "no value".
  selectedNominalCompositionName: string | null = null; // NOTE: May not have been set at all.
  
  //
  // Attributes related to the DataTable component for Runs
  //
  //////////////////////////////////////////////////////

  runsTableData: Run[] = [];
  runSelectedKey = "id"
  selectedRunId: number | null = null;
  selectedRunNumber: number | null = null;

  runsTableColumns: TableColumn[] = [
    { key: 'run_number', label: 'Run #', align: 'right' },
    { key: 'status', label: 'Status', align: 'center'  },
    { key: 'created_at', label: 'Created at', align: 'right', type: 'date', dateFormat: 'short' },
    { key: 'created_by', label: 'Created by' },
    { key: 'updated_at', label: 'Updated at', align: 'right', type: 'date', dateFormat: 'short' },
    { key: 'updated_by', label: 'Updated by' },
    { key: 'started_at', label: 'Started at', align: 'right', type: 'date', dateFormat: 'short' },
    { key: 'completed_at', label: 'Completed at', align: 'right', type: 'date', dateFormat: 'short' }
  ];

  //
  // Attributes related to the DataTable component for SubRuns
  //
  //////////////////////////////////////////////////////

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

  //
  // Attributes related to the DataTable component for SimulationArtifacts
  //
  //////////////////////////////////////////////////////

  simulationArtifactsTableData: SimulationArtifact[] = [];

  simulationArtifactsTableColumns: TableColumn[] = [
    { key: 'artifact_type', label: 'Artifact Type', align: 'center'  },
    { key: 'file_path', label: 'File Path' },
    { key: 'file_size', label: 'File Size' },
    { key: 'checksum', label: 'Checksum' }
  ];

  //
  // Attributes for the rest of UI components
  //
  //////////////////////////////////////////////////////

  nRunsToSchedule: number = 1;

  activeTab: 'tab1' | 'tab2' = 'tab1';

  //
  // Component related methods
  //
  //////////////////////////////////////////////////////

  constructor(// NOTE: All these dependencies will be injected.
    private nominalCompositionService: NominalCompositionService,
    private runService: RunService,
    private subRunService: SubRunService,
    private simulationArtifactService: SimulationArtifactService,
    private dataOpsService: DataOpsService
  ) {}

  /* 
   * NOTE: This method is a lifecycle hook defined by the OnInit interface. It is 
   *       automatically called by Angular once, just after the component is 
   *       created and initialized, but before it's displayed.
  */
  ngOnInit(): void {

    this.startedServiceRequest()

    // Loading the Nominal Compositions for the corresponding drop-down
    this.nominalCompositionService.getAll().pipe(
      finalize(() => {
        this.finalizedServiceRequest();
      })
    ).subscribe({
      next: (data) => {
        this.allNominalCompositions = data
      },
      error: (err) => {
        console.error('Failed to fetch Nominal Compositions', err)
        this.serviceRequestErrorMessage = `Failed to fetch Nominal Compositions. Error: ${err?.error?.message}`;
      }
    });

  }

  cleanMessages(): void {
    this.serviceRequestErrorMessage = null;
    this.scheduleSuccessMessage = null;
  }

  startedServiceRequest(): void {
    this.serviceRequestOn = true;
    this.cleanMessages();
  }

  finalizedServiceRequest(): void {
    this.serviceRequestOn = false;
  }

  //
  // Methods related to the two tabs
  //
  //////////////////////////////////////////////////////

  activateTab2(): void {

    this.activeTab = 'tab2';

    if (this.selectedNominalCompositionId) {
      this.fetchRunsForSelectedComposition();
      this.cleanMessages();
    }

  }

  //
  // Methods related to input "Select Nominal Composition"
  //
  //////////////////////////////////////////////////////

  onNominalCompositionSelected(nominalId: number): void {

    // NOTE: The + coerces nominalId to a number
    this.selectedNominalCompositionId = nominalId;
    this.selectedNominalCompositionName = this.allNominalCompositions.find(nc => nc.id === +nominalId)?.name ?? null;

    if (this.activeTab === 'tab2') {
      this.fetchRunsForSelectedComposition();
      this.cleanRunsInfo();
      this.cleanMessages();
    }

  }

  private fetchRunsForSelectedComposition(): void {

    if (!this.selectedNominalCompositionId) return;

    this.startedServiceRequest();

    this.runService.getAll(this.selectedNominalCompositionId).pipe(
      finalize(() => {
        this.finalizedServiceRequest();
      })
    ).subscribe({
        next: (data) => {
          this.runsTableData = data
        },
        error: (err) => {
          console.error('Failed to fetch Runs', err);
          this.serviceRequestErrorMessage = `Failed to fetch Runs. Error: ${err?.error?.message}`;
          this.cleanRunsInfo();
        }
      });

  }

  //
  // Methods related to input "Number of Calculations"
  //
  //////////////////////////////////////////////////////

  // Increases the number of Runs to schedule up to 5
  incrementNRuns(): void {
    if (this.nRunsToSchedule < 5) this.nRunsToSchedule++;
  }

  // Decreases the number of Runs to schedule down to 1
  decrementNRuns(): void {
    if (this.nRunsToSchedule > 1) this.nRunsToSchedule--;
  }  

  // Schedule Runs
  scheduleRuns(): void {

    this.startedServiceRequest();

    const payload = { numSimulations: this.nRunsToSchedule };

    this.dataOpsService.generate_explore(this.selectedNominalCompositionName ?? "", payload).pipe(
      finalize(() => {
        this.finalizedServiceRequest();
      })
    ).subscribe({
      next: () => {
        this.scheduleSuccessMessage = `'${this.nRunsToSchedule}' Run(s) have been scheduled for 
        Nominal Composition '${this.selectedNominalCompositionName}'. 
        Check in the tab 'View all scheduled runs'.`;
      },
      error: (err) => {
        this.serviceRequestErrorMessage = `Failed to schedule Runs for 
        Nominal Composition '${this.selectedNominalCompositionName}'. Error: ${err?.error?.message}`;
        console.error('Pre-Deployment Exploration error:', err);
      }
    });

  }

  //
  // Methods related to the Runs Data Table
  //
  //////////////////////////////////////////////////////

  onRunsTableRowSelected(runId: number | string | null): void {

    // This also cleans the Simulation Artifact Data Table
    this.cleanSubRunsInfo();

    // When user deselects
    if (this.selectedRunId === runId) {
      this.selectedRunId = null;
      this.selectedRunNumber = null;
    } else {
      // When user selects
      this.selectedRunId = runId as number;
      this.selectedRunNumber = this.runsTableData.find(run => run.id === runId)?.run_number ?? null;
      this.fetchSubRunsForSelectedRun();
    }

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

  private cleanRunsInfo() {

    this.runsTableData = [];
    this.selectedRunId = null;
    this.selectedRunNumber = null;
    this.cleanSubRunsInfo();

  }

  //
  // Methods related to the SubRuns DataTable
  //
  //////////////////////////////////////////////////////

  onSubRunsTableRowSelected(subRunId: number | string | null): void {

    this.cleanSimulationArtifactsInfo();

    // When user deselects
    if (this.selectedSubRunId === subRunId) {
      this.selectedSubRunId = null;
      this.selectedSubRunNumber = null;
    } else {
      // When user selects
      this.selectedSubRunId = subRunId as number;
      this.selectedSubRunNumber = this.subRunsTableData.find(subRun => subRun.id === subRunId)?.sub_run_number ?? null;
      this.fetchSimulationArtifactsForSelectedSubRun();
    }

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

    private cleanSubRunsInfo() {

      this.subRunsTableData = [];
      this.selectedSubRunId = null;
      this.selectedSubRunNumber = null;
      this.cleanSimulationArtifactsInfo();

  }

  private cleanSimulationArtifactsInfo() {
      this.simulationArtifactsTableData = [];
  }

}
