package org.doi.prmv4p113603.mlops.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionDto;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExploitationRequest;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExplorationRequest;
import org.doi.prmv4p113603.mlops.domain.*;
import org.doi.prmv4p113603.mlops.exception.SimulationArtifactNotFoundException;
import org.doi.prmv4p113603.mlops.exception.SimulationDirectoryNotFoundException;
import org.doi.prmv4p113603.mlops.model.*;
import org.doi.prmv4p113603.mlops.repository.*;
import org.doi.prmv4p113603.mlops.util.SimulationArtifactFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
    public NominalCompositionDto scheduleExploration(String nominalCompositionName, ScheduleExplorationRequest request) {

        // Check that the NominalComposition exists and that its directory exists
        NominalComposition nominalComposition = compositionRepo.findByName(nominalCompositionName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nominal Composition not found"));

        // Now loading and checking the consistency of directories

        int nextRunNumber = runRepo.findMaxRunNumberByNominalCompositionId(nominalComposition.getId()).orElse(0) + 1;

        SimulationDirectories simulationDirectories = simulationDirectoriesFactory.create(
                SimulationType.EXPLORATION,
                nominalCompositionName,
                nextRunNumber,
                request.getNumSimulations());

        try {
            simulationDirectories.load();
        } catch (SimulationDirectoryNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }

        // Since everything is OK with the folders, persisting the Runs to DB
        List<Run> runs = new ArrayList<>();
        for (SimulationDirectory runDir : simulationDirectories.getNominalCompositionDir().getChildren()) {

            Run run = Run.builder()
                    .nominalComposition(nominalComposition)
                    .runNumber(runDir.getNumber())
                    .status(SimulationStatus.SCHEDULED)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            SubRun subRun = SubRun.builder() // A Run is created with a reference structure as SubRun 0
                    .run(run)
                    .subRunNumber(0)
                    .status(SimulationStatus.SCHEDULED)
                    .scheduledAt(Instant.now())
                    .build();

            run.setSubRuns(List.of(subRun));

            try {
                subRun.setSimulationArtifacts(SimulationArtifactFactory.load(
                        nominalCompositionName,
                        subRun,
                        runDir.getChildren().get(0))); // passing the SubRun directory
            } catch (SimulationArtifactNotFoundException ex) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
            }

            runs.add(run);

        }

        // And finally uploading to MinIO
        // TODO: call simulationDirectories.upload(); and handle exception

        // Returning only DTOs
        return NominalCompositionDto.fromScheduleExploreExploitRequest(nominalComposition, runs);

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
    public NominalCompositionDto scheduleExploitation(String nominalCompositionName, ScheduleExploitationRequest request) {

        // Check that the NominalComposition exists and that its directory exists
        NominalComposition nominalComposition = compositionRepo.findByName(nominalCompositionName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nominal Composition not found"));

        /*
         * Validate the input payload against the database. Checking if
         * all Runs exist and that all corresponding SubRuns are new records.
         */

        // Check Runs IDs
        List<Long> runIds = request.getRuns().stream()
                .map(ScheduleExploitationRequest.RunInput::getId)
                .toList();

        List<Run> existingRuns = runRepo.findAllByIdIn(runIds).stream().toList();

        Set<Long> existingRunIds = existingRuns.stream()
                .map(Run::getId)
                .collect(Collectors.toSet());

        for (Long requestedId : runIds) {
            if (existingRunIds.contains(requestedId)) {
                EntityNotFoundException ex = new EntityNotFoundException("Run with ID " + requestedId + " not found");
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
                SimulationType.EXPLOITATION,
                nominalCompositionName,
                request.getRuns());

        try {
            simulationDirectories.load();
        } catch (SimulationDirectoryNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }

        // Since everything is OK with the folders, persisting the Runs to DB
        Map<Integer, Run> runsByNumber = existingRuns.stream()
                .collect(Collectors.toMap(Run::getRunNumber, Function.identity()));
        for (SimulationDirectory runDir : simulationDirectories.getNominalCompositionDir().getChildren()) {

            Run run = runsByNumber.get(runDir.getNumber());

            List<SubRun> newSubRuns = new ArrayList<>();
            for (SimulationDirectory subRunDir : runDir.getChildren()) {

                SubRun subRun = SubRun.builder()
                        .run(run)
                        .subRunNumber(subRunDir.getNumber())
                        .status(SimulationStatus.SCHEDULED)
                        .scheduledAt(Instant.now())
                        .build();

                try {
                    subRun.setSimulationArtifacts(SimulationArtifactFactory.load(
                            nominalCompositionName,
                            subRun,
                            subRunDir));
                } catch (SimulationArtifactNotFoundException ex) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
                }

                newSubRuns.add(subRun);

            }

            run.setSubRuns(newSubRuns);

        }

        // And finally uploading to MinIO
        // TODO: call simulationDirectories.upload(); and handle exception

        // Returning only DTOs
        return NominalCompositionDto.fromScheduleExploreExploitRequest(nominalComposition, existingRuns);

    }

}
