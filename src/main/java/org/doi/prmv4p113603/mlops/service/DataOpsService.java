package org.doi.prmv4p113603.mlops.service;

import org.doi.prmv4p113603.mlops.config.MlopsProperties;
import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionDto;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExplorationRequest;
import org.doi.prmv4p113603.mlops.domain.SimulationDirectories;
import org.doi.prmv4p113603.mlops.domain.SimulationDirectory;
import org.doi.prmv4p113603.mlops.domain.SimulationStatus;
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

/**
 * Service layer scoped to the Data Generation & Labeling (DataOps) phase,
 * specifically the Generate (DataOps phase; exploration/exploitation) step.
 * <p>
 * It encapsulates all business logic related to configuration space exploration
 * for a given Nominal Composition and is responsible for scheduling data generation
 * jobs and detecting simulation artifacts.
 */
@Service
public class DataOpsService {

    // Dependencies
    private final NominalCompositionRepository compositionRepo;
    private final RunRepository runRepo;
    private final MlopsProperties mlopsProperties;

    public DataOpsService(NominalCompositionRepository compositionRepo, RunRepository runRepo, MlopsProperties mlopsProperties) {

        this.compositionRepo = compositionRepo;
        this.runRepo = runRepo;
        this.mlopsProperties = mlopsProperties;

    }

    /**
     * Schedules exploration jobs for a given nominal composition by detecting local simulation artifacts.
     * <p>
     * This service method initiates a new Run and creates SubRuns and SimulationArtifacts for each valid folder found.
     * <p>
     * Example POST request:
     * POST /api/v1/dataops/generate/Zr49Cu49Al2
     * Body: { "numSimulations": 3 }
     *
     * @param nominalCompositionName the name of the nominal composition
     * @param request                payload indicating the number of sub-runs to process
     * @return created NominalCompositionDto with run, sub-run, and artifact information
     */
    public NominalCompositionDto scheduleExploration(String nominalCompositionName, ScheduleExplorationRequest request) {

        // Check that the NominalComposition exists and that its directory exists
        NominalComposition nominalComposition = compositionRepo.findByName(nominalCompositionName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nominal Composition not found"));

        // Now loading and checking directories
        int nextRunNumber = runRepo.findMaxRunNumberByNominalCompositionId(nominalComposition.getId()).orElse(0) + 1;

        SimulationDirectories simulationDirectories =
                createSimulationDirectories(nominalCompositionName, mlopsProperties.getDataRoot(), nextRunNumber, request.getNumSimulations());

        try {
            simulationDirectories.load(true);
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

        // Returning only DTOs
        return NominalCompositionDto.fromScheduleExploreExploitRequest(nominalComposition, runs);

    }

    // Will be accessible from test class that is in the same package
    protected SimulationDirectories createSimulationDirectories(String name, String root, int run, int count) {

        SimulationDirectories dirs = new SimulationDirectories(name, root);
        dirs.setNextRunNumber(run);
        dirs.setNumSimulations(count);

        return dirs;

    }

}
