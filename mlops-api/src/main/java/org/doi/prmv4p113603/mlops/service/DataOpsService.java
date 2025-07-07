package org.doi.prmv4p113603.mlops.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.common.service.MinioStorageService;
import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionDto;
import org.doi.prmv4p113603.mlops.data.dto.SimulationArtifactDto;
import org.doi.prmv4p113603.mlops.data.request.AirflowDagRunRequest;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExploitationRequest;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExplorationRequest;
import org.doi.prmv4p113603.mlops.data.response.ExplorationPipelineRunResponse;
import org.doi.prmv4p113603.mlops.domain.*;
import org.doi.prmv4p113603.mlops.exception.DataOpsInternalInconsistencyException;
import org.doi.prmv4p113603.mlops.exception.NominalCompositionNotFoundException;
import org.doi.prmv4p113603.mlops.exception.SimulationArtifactNotFoundException;
import org.doi.prmv4p113603.mlops.model.*;
import org.doi.prmv4p113603.mlops.repository.*;
import org.doi.prmv4p113603.common.util.MinioUtils;
import org.doi.prmv4p113603.mlops.util.SimulationArtifactFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service layer scoped to the Data Generation & Labeling (DataOps) phase,
 * specifically the Generate (DataOps phase; exploration/exploitation) step.
 * <p>
 * It encapsulates all business logic related to configuration space exploration
 * for a given Nominal Composition and is responsible for scheduling data generation
 * jobs and detecting simulation artifacts.
 */
@Service
@AllArgsConstructor
public class DataOpsService {

    // Dependencies
    private final NominalCompositionRepository compositionRepo;
    private final SimulationDirectoriesFactory simulationDirectoriesFactory;
    private final MinioStorageService minioStorageService;
    private final ExplorationPipelineRunRepository explorationPipelineRunRepository;
    private final RunRepository runRepo;
    private final SubRunRepository subRunRepo;
    private final WebClient airflowWebClient;

    /**
     * Schedules exploration jobs for a given nominal composition by detecting local simulation artifacts.
     * <p>
     * This service method initiates a new Run and creates SubRuns and SimulationArtifacts for each valid folder found.
     * <p>
     * Example POST request:
     * POST /api/v1/dataops/generate/Zr49Cu49Al2
     * Body:
     * <pre>
     * {
     *   "numSimulations": 3
     * }
     * </pre>
     *
     * @param nominalCompositionName the name of the nominal composition
     * @param request                payload indicating the number of sub-runs to process
     * @return created NominalCompositionDto with run, sub-run, and artifact information
     */
    @Transactional
    public NominalCompositionDto scheduleExploration(String nominalCompositionName, ScheduleExplorationRequest request) {

        // Check that the NominalComposition exists and that its directory exists
        NominalComposition nominalComposition = compositionRepo.findByName(nominalCompositionName)
                .orElseThrow(() -> new NominalCompositionNotFoundException(nominalCompositionName));

        // Now loading and checking the consistency of directories

        int nextRunNumber = runRepo.findMaxRunNumberByNominalCompositionId(nominalComposition.getId()).orElse(0) + 1;

        SimulationDirectories simulationDirectories = simulationDirectoriesFactory.create(
                SimulationType.GENERATE_EXPLORATION,
                nominalCompositionName,
                nextRunNumber,
                request.getNumSimulations());

        simulationDirectories.load();

        // Since everything is OK with the folders, persisting the Runs to DB ...
        List<Run> runs = new ArrayList<>();
        for (SimulationDirectory runDir : simulationDirectories.getNominalCompositionDir().getChildren()) {

            Run run = Run.builder()
                    .nominalComposition(nominalComposition)
                    .runNumber(runDir.getNumber())
                    .status(SimulationStatus.SCHEDULED)
                    .build();

            SubRun subRun = SubRun.builder() // A Run is created with a reference structure as SubRun 0
                    .run(run)
                    .subRunNumber(0)
                    .status(SimulationStatus.SCHEDULED)
                    .build();

            run.setSubRuns(List.of(subRun));

            subRun.setSimulationArtifacts(SimulationArtifactFactory.load(
                    nominalCompositionName,
                    subRun,
                    runDir.getChildren().get(0),  // passing the SubRun (reference structure) directory
                    SimulationArtifactRole.GENERATE_IO));

            runs.add(run);

        }

        /*
         * NOTE: Regarding exception handling here, the application has an exception handler
         *  annotated with @ControllerAdvice. Since this service is expose via a REST controller,
         *  that exception handler ensures consistent API error responses.
         */
        // Persisting ensuring atomicity
        runRepo.saveAll(runs);

        // Creating a DTO
        NominalCompositionDto ncDto = NominalCompositionDto.fromScheduleExplorationRequest(nominalComposition, runs);

        // Uploading to MinIO
        uploadSimulationInputFilesToS3(ncDto);

        // Finally triggering the Airflow DAG asynchronously
        // TODO: SOAP parameters in a configuration properties or set in the UI
        AirflowDagRunRequest.SoapParameters soapParameters = AirflowDagRunRequest.SoapParameters.builder()
                .cutoff(3.75)
                .lMax(6)
                .nMax(8)
                .nZ(3)
                .z("{13 29 40}")
                .nSpecies(3)
                .ZSpecies("{13 29 40}")
                .build();

        // The set of jobs that will be submitted to the HPC service by an Airflow DAG task
        List<AirflowDagRunRequest.RunJob> payloadRuns = new ArrayList<>();
        runs.stream()
                .flatMap(run -> run.getSubRuns().stream())
                .filter(subRun -> subRun.getSubRunNumber() == 0)
                .forEach(subRun -> payloadRuns.add(getExploreRequestPayloadRunJob(subRun)));

        AirflowDagRunRequest requestPayload = AirflowDagRunRequest.buildFrom(
                nominalCompositionName,
                payloadRuns,
                soapParameters,
                "string" // TODO: could be defined in the UI
        );

        try {
            System.out.println(requestPayload.toJson());
        } catch (Exception e) {
            throw new DataOpsInternalInconsistencyException(e.getMessage());
        }

        // Triggering the Airflow DAG
        // TODO: 'pre_deployment_exploration' in application.properties?
        ExplorationPipelineRunResponse response = triggerAirflowDag("pre_deployment_exploration", requestPayload);

        ExplorationPipelineRun epRun = ExplorationPipelineRun.builder()
                .responsePayload(response)
                .runs(runs)
                .build();

        explorationPipelineRunRepository.save(epRun);

        // Returning only DTOs
        return ncDto;

    }

    /**
     * Schedules geometry augmentation (exploitation) for a given nominal composition and runs.
     * <p>
     * This service method creates additional SubRuns and SimulationArtifacts for each valid folder found.
     * <p>
     * Example POST request:
     * POST /api/v1/dataops/generate/Zr49Cu49Al2/augment
     * Body:
     * <pre>
     * { "runs": [
     *   {
     *     "id": 1,
     *     "run_number": 1,
     *     "sub_runs": [1,2,3,4,5]
     *   },
     *   {
     *     "id": 2,
     *     "run_number": 2,
     *     "sub_runs": [6,7,13,14]
     *   }
     * ]}
     * </pre>
     *
     * @param nominalCompositionName the name of the nominal composition
     * @param request                payload indicating the set of runs and sub-runs to process
     * @return created NominalCompositionDto with run, sub-run, and artifact information
     */
    @Transactional
    public NominalCompositionDto scheduleExploitation(String nominalCompositionName, ScheduleExploitationRequest request) {

        // Check that the NominalComposition exists and that its directory exists
        NominalComposition nominalComposition = compositionRepo.findByName(nominalCompositionName)
                .orElseThrow(() -> new NominalCompositionNotFoundException(nominalCompositionName));

        /*
         * Validate the input payload against the database. Checking if
         * all Runs exist and that all corresponding SubRuns are new records.
         */

        // Check Runs IDs
        List<Long> requestedRunsIds = request.getRuns().stream()
                .map(ScheduleExploitationRequest.RunInput::getId)
                .toList();

        List<Run> existingRequestedRuns = runRepo.findAllByIdIn(requestedRunsIds).stream().toList();

        Set<Long> requestedExistingRunsIds = existingRequestedRuns.stream()
                .map(Run::getId)
                .collect(Collectors.toSet());

        for (Long requestedRunId : requestedRunsIds) {
            if (!requestedExistingRunsIds.contains(requestedRunId)) {
                EntityNotFoundException ex = new EntityNotFoundException("Run with ID " + requestedRunId + " not found");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
            }
        }

        // Check SubRuns IDs
        for (ScheduleExploitationRequest.RunInput runInput : request.getRuns()) {

            boolean exists = subRunRepo.existsAnyByRunIdAndSubRunNumbers(runInput.getId(), runInput.getSubRuns());

            if (exists) {
                IllegalStateException ex = new IllegalStateException("Some SubRuns already exist for run ID " + runInput.getId());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
            }

        }

        // Checking the consistency of directories

        SimulationDirectories simulationDirectories = simulationDirectoriesFactory.create(
                SimulationType.GENERATE_EXPLOITATION,
                nominalCompositionName,
                request.getRuns());

        simulationDirectories.load();

        // Since everything is OK with the folders, persisting the Runs to DB ...
        Map<Integer, Run> existingRequestedRunsByNumber = existingRequestedRuns.stream()
                .collect(Collectors.toMap(Run::getRunNumber, Function.identity()));

        List<SubRun> allNewSubRuns = new ArrayList<>();
        for (SimulationDirectory runDir : simulationDirectories.getNominalCompositionDir().getChildren()) {

            Run run = existingRequestedRunsByNumber.get(runDir.getNumber());

            for (SimulationDirectory subRunDir : runDir.getChildren()) {

                SubRun subRun = SubRun.builder()
                        .run(run)
                        .subRunNumber(subRunDir.getNumber())
                        .status(SimulationStatus.SCHEDULED)
                        .createdAt(Instant.now())
                        .build();

                subRun.setSimulationArtifacts(SimulationArtifactFactory.load(
                        nominalCompositionName,
                        subRun,
                        subRunDir,
                        SimulationArtifactRole.GENERATE_IO));

                allNewSubRuns.add(subRun);

            }

        }

        // Persisting ensuring atomicity
        subRunRepo.saveAll(allNewSubRuns);

        // Creating a DTO
        NominalCompositionDto ncDto = NominalCompositionDto.fromScheduleExploitationRequest(nominalComposition,
                existingRequestedRuns,
                allNewSubRuns);

        // Finally uploading to MinIO
        uploadSimulationInputFilesToS3(ncDto); // TODO: only inputs

        // TODO: trigger the Airflow DAG

        // Returning only DTOs
        return ncDto;

    }

    /*
     * Helpers
     *
     * TODO: refactor to create some components (@Component); especially for efficient testting of
     *  HTTP calls made to another services like Airflow DAG trigger.
     */

    private void uploadSimulationInputFilesToS3(NominalCompositionDto nominalCompositionDto) {

        nominalCompositionDto.getRuns().stream()
                .flatMap(runDto -> runDto.getSubRuns().stream())
                .flatMap(subRunDto -> subRunDto.getSimulationArtifacts().stream())
                .filter(artifactDto -> artifactDto.getArtifactRole().isGenerateInput()) // only input files
                .map(SimulationArtifactDto::getFilePath)
                .forEach(path -> minioStorageService.uploadFile(MinioUtils.pathToKey(path), path));
    }

    public ExplorationPipelineRunResponse triggerAirflowDag(String dagId, AirflowDagRunRequest payload) {
        String uri = String.format("/dags/%s/dagRuns", dagId);

        return airflowWebClient.post()
                .uri(uri)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(ExplorationPipelineRunResponse.class)
                .doOnNext(res -> System.out.println("DAG Run triggered: {}" + res.getDagId()))
                .block(); // Only block if in a sync context

    }

    public ExplorationPipelineRunResponse getAirflowDagRunStatus(String dagId, String dagRunId) {
        String uri = String.format("/dags/%s/dagRuns/%s", dagId, dagRunId);

        return airflowWebClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(ExplorationPipelineRunResponse.class)
                .doOnNext(res -> System.out.println("DAG Run status: {}" + res.getDagRunId()))
                .block(); // Only block if in a sync context

    }

    private static AirflowDagRunRequest.RunJob getExploreRequestPayloadRunJob(SubRun subRun) {

        // LAMMPS
        SimulationArtifact lammpsInput = getSimulationInput(subRun, SimulationArtifactType.LAMMPS_INPUT);

        Set<String> matchingLammpsOutputs = getSimulationOutputs(subRun, Set.of(
                SimulationArtifactType.LAMMPS_DUMP,
                SimulationArtifactType.LAMMPS_LOG,
                SimulationArtifactType.LAMMPS_OUTPUT
        ));

        AirflowDagRunRequest.Job jobLammps = AirflowDagRunRequest.Job.builder()
                .inputFile(lammpsInput.getFilePath())
                .outputFiles(matchingLammpsOutputs.stream().toList())
                .build();

        // Quantum Espresso
        SimulationArtifact qeInput = getSimulationInput(subRun, SimulationArtifactType.QE_SCF_IN);

        Set<String> matchingQeOutputs = getSimulationOutputs(subRun, Set.of(
                SimulationArtifactType.QE_SCF_OUT,
                SimulationArtifactType.LAMMPS_DUMP_XYZ
        ));

        AirflowDagRunRequest.Job jobQe = AirflowDagRunRequest.Job.builder()
                .inputFile(qeInput.getFilePath())
                .outputFiles(matchingQeOutputs.stream().toList())
                .build();

        // LOBSTER
        SimulationArtifact lobInput = getSimulationInput(subRun, SimulationArtifactType.LOBSTER_INPUT);

        Set<String> matchingLobOutputs = getSimulationOutputs(subRun, Set.of(
                SimulationArtifactType.LOBSTER_OUTPUT,
                SimulationArtifactType.LOBSTER_RUN_OUTPUT,
                SimulationArtifactType.ICOHPLIST
        ));

        AirflowDagRunRequest.Job jobLob = AirflowDagRunRequest.Job.builder()
                .inputFile(lobInput.getFilePath())
                .outputFiles(matchingLobOutputs.stream().toList())
                .build();

        return AirflowDagRunRequest.RunJob.builder()
                .runNumber(subRun.getRun().getRunNumber())
                .jobs(List.of(jobLammps, jobQe, jobLob))
                .build();

    }

    private static SimulationArtifact getSimulationInput(SubRun subRun,
                                                         SimulationArtifactType simulationArtifactType) { // TODO: throws???

        return subRun.getSimulationArtifacts().stream()
                .filter(artifact -> artifact.getArtifactType() == simulationArtifactType)
                .findFirst()
                .orElseThrow(() -> new SimulationArtifactNotFoundException(simulationArtifactType.toString()));

    }

    private static Set<String> getSimulationOutputs(SubRun subRun,
                                                    Set<SimulationArtifactType> jobLammpsOutputTypes) { // TODO: throws???

        return subRun.getSimulationArtifacts().stream()
                .filter(artifact -> jobLammpsOutputTypes.contains(artifact.getArtifactType()))
                .map(SimulationArtifact::getFilePath)
                .collect(Collectors.toSet());

    }

}
