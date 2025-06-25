import { Component, OnInit } from '@angular/core';

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

  // Messages related to data operations
  dataError: string | null = null;
  loadingData = false;
  scheduleError: string | null = null;
  scheduleSuccess: string | null = null;

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
    { key: 'sub_run_number', label: 'SubRun #', align: 'right' },
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

    this.loadingData = true;

    this.nominalCompositionService.getAll().subscribe({
      next: (data) => {
        this.allNominalCompositions = data
        this.loadingData = false;
      },
      error: (err) => {
        console.error('Failed to load Nominal Compositions', err)
        this.dataError = 'Failed to load Nominal Compositions';
        this.loadingData = false;
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

    const payload = { numSimulations: this.nRunsToSchedule };

      this.dataOpsService.generate(this.selectedNominalCompositionName ?? "", payload).subscribe({
        next: () => {
          this.scheduleSuccess = `'${this.nRunsToSchedule}' Runs have been scheduled for 
          Nominal Composition '${this.selectedNominalCompositionName}'. 
          Check in the tab 'View all scheduled runs'.`;
          // TODO: display the scheduled results in a Data Table?
        },
        error: (err) => {
          this.scheduleError = `Failed to schedule Runs for 
          Nominal Composition '${this.selectedNominalCompositionName}'.`;
          console.error(err);
        }
      });

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
    }

  }

  private fetchRunsForSelectedComposition(): void {

    if (!this.selectedNominalCompositionId) return;

    this.loadingData = true;

    this.runService.getAll(this.selectedNominalCompositionId)
      .subscribe({
        next: (data) => {
          this.runsTableData = data
          this.loadingData = false;
        },
        error: (err) => {
          console.error('Failed to fetch Runs', err);
          this.dataError = 'Failed to fetch Runs';
          this.runsTableData = [];
          this.loadingData = false;
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
  // Methods related to the two tabs
  //
  //////////////////////////////////////////////////////

  activateTab2(): void {

    this.activeTab = 'tab2';

    if (this.selectedNominalCompositionId) {
      this.fetchRunsForSelectedComposition();
    }

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

    this.loadingData = true;

    this.subRunService.getAll(this.selectedRunId)
      .subscribe({
        next: (data) => {
          this.subRunsTableData = data
          this.loadingData = false;
        },
        error: (err) => {
          console.error('Failed to fetch SubRuns', err);
          this.dataError = 'Failed to fetch SubRuns';
          this.subRunsTableData = [];
          this.loadingData = false;
        }
      });

  }

  private cleanSubRunsInfo() {

      this.subRunsTableData = [];
      this.selectedSubRunId = null;
      this.selectedSubRunNumber = null;
      this.simulationArtifactsTableData = [];
      this.cleanSimulationArtifactsInfo();

  }

  //
  // Methods related to the SubRuns DataTable
  //
  //////////////////////////////////////////////////////

  onSubRunsTableRowSelected(genericSubRunId: number | string | null): void {
    
    const subRunId = typeof genericSubRunId === 'string' ? +genericSubRunId : genericSubRunId;

    // This is to make the table single-selection
    if (this.selectedSubRunId === subRunId) {
      this.selectedSubRunId = null;
      this.selectedSubRunNumber = null;
      this.cleanSimulationArtifactsInfo();
      return;
    }

    // Defining current state
    this.selectedSubRunId = subRunId;
    this.selectedSubRunNumber = this.subRunsTableData.find(srun => srun.id === subRunId)?.sub_run_number ?? null;

    this.fetchSimulationArtifactsForSelectedSubRun();

  }

  private fetchSimulationArtifactsForSelectedSubRun(): void {

    if (!this.selectedSubRunId) return;

    this.loadingData = true;

    this.simulationArtifactService.getAll(this.selectedSubRunId)
      .subscribe({
        next: (data) => {
          this.simulationArtifactsTableData = data
          this.loadingData = false;
        },
        error: (err) => {
          console.error('Failed to fetch Simulation Artifacts', err);
          this.dataError = 'Failed to fetch Simulation Artifacts';
          this.simulationArtifactsTableData = [];
          this.loadingData = false;
        }
      });

  }

  private cleanSimulationArtifactsInfo() {
      this.simulationArtifactsTableData = [];
  }

}
