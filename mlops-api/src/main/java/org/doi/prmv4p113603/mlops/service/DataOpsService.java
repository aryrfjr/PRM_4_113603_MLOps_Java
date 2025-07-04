package org.doi.prmv4p113603.mlops.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.common.service.MinioStorageService;
import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionDto;
import org.doi.prmv4p113603.mlops.data.dto.SimulationArtifactDto;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExploitationRequest;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExplorationRequest;
import org.doi.prmv4p113603.mlops.domain.*;
import org.doi.prmv4p113603.mlops.exception.NominalCompositionNotFoundException;
import org.doi.prmv4p113603.mlops.model.*;
import org.doi.prmv4p113603.mlops.repository.*;
import org.doi.prmv4p113603.common.util.MinioUtils;
import org.doi.prmv4p113603.mlops.util.SimulationArtifactFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final RunRepository runRepo;
    private final SubRunRepository subRunRepo;

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
                    runDir.getChildren().get(0),  // passing the SubRun directory
                    SimulationArtifactRole.GENERATE_INPUT)); // only input files

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

        // Finally uploading to MinIO
        uploadSimulationInputFilesToS3(ncDto);

        /*
         * TODO: trigger the Airflow DAG passing three different jobs with inputs/outputs:
         *
         *  - SimulationArtifactType.LAMMPS_INPUT (e.g. /data/ML/big-data-full/Zr47Cu47Al6/c/md/lammps/100/1/Zr47Cu47Al6.lmp.inp):
         *   -- SimulationArtifactType.LAMMPS_DUMP; (e.g. /data/ML/big-data-full/Zr47Cu47Al6/c/md/lammps/100/1/zca-th300.dump)
         *   -- SimulationArtifactType.LAMMPS_LOG; (e.g. /data/ML/big-data-full/Zr47Cu47Al6/c/md/lammps/100/1/log.lammps)
         *   -- SimulationArtifactType.LAMMPS_OUTPUT; (e.g. /data/ML/big-data-full/Zr47Cu47Al6/c/md/lammps/100/1/Zr47Cu47Al6.lmp.out)
         *
         *  - SimulationArtifactType.QE_SCF_IN: (e.g. /data/ML/big-data-full/Zr47Cu47Al6/c/md/lammps/100/1/2000/0/Zr47Cu47Al6.scf.in)
         *   -- SimulationArtifactType.QE_SCF_OUT; (e.g. /data/ML/big-data-full/Zr47Cu47Al6/c/md/lammps/100/1/2000/0/Zr47Cu47Al6.scf.out)
         *   -- SimulationArtifactType.QE_SCF_OUT; (e.g. /data/ML/big-data-full/Zr47Cu47Al6/c/md/lammps/100/1/2000/0/Zr47Cu47Al6.xyz)
         *
         *  - SimulationArtifactType.LOBSTER_INPUT: (e.g., /data/ML/big-data-full/Zr47Cu47Al6/c/md/lammps/100/1/2000/0/lobsterin)
         *   -- SimulationArtifactType.LOBSTER_OUTPUT; (e.g. /data/ML/big-data-full/Zr47Cu47Al6/c/md/lammps/100/1/2000/0/Zr47Cu47Al6.lb.out)
         *   -- SimulationArtifactType.LOBSTER_RUN_OUTPUT; (e.g. /data/ML/big-data-full/Zr47Cu47Al6/c/md/lammps/100/1/2000/0/lobsterout)
         *   -- SimulationArtifactType.ICOHPLIST; (e.g. /data/ML/big-data-full/Zr47Cu47Al6/c/md/lammps/100/1/2000/0/ICOHPLIST.lobster)
         */

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
                        SimulationArtifactRole.GENERATE_INPUT)); // only input files

                allNewSubRuns.add(subRun);

            }

        }

        // Persisting ensuring atomicity
        subRunRepo.saveAll(allNewSubRuns);

        // Creating a DTO
        NominalCompositionDto ncDto = NominalCompositionDto.fromScheduleExploitationRequest(nominalComposition, existingRequestedRuns, allNewSubRuns);

        // Finally uploading to MinIO
        uploadSimulationInputFilesToS3(ncDto);

        // TODO: trigger the Airflow DAG

        // Returning only DTOs
        return ncDto;

    }

    /*
     * Helpers
     */

    private void uploadSimulationInputFilesToS3(NominalCompositionDto nominalCompositionDto) {

        nominalCompositionDto.getRuns().stream()
                .flatMap(runDto -> runDto.getSubRuns().stream())
                .flatMap(subRunDto -> subRunDto.getSimulationArtifacts().stream())
                .filter(artifactDto -> artifactDto.getArtifactRole().isGenerateInput()) // only input files
                .map(SimulationArtifactDto::getFilePath)
                .forEach(path -> minioStorageService.uploadFile(MinioUtils.pathToKey(path), path));
    }

}
