package org.doi.prmv4p113603.mlops.service;

import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionDto;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExplorationRequest;
import org.doi.prmv4p113603.mlops.model.*;
import org.doi.prmv4p113603.mlops.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
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

    // Constants
    private static final String DATA_ROOT = "/data/ML/big-data-full"; // TODO: environment variable from docker-compose.yml

    // Dependencies
    private final FileSystemService fileSystem;
    private final NominalCompositionRepository compositionRepo;
    private final RunRepository runRepo;


    public DataOpsService(
            NominalCompositionRepository compositionRepo,
            RunRepository runRepo,
            FileSystemService fileSystem) {
        this.compositionRepo = compositionRepo;
        this.runRepo = runRepo;
        this.fileSystem = fileSystem;
    }

    public NominalCompositionDto scheduleExploration(String nominalCompositionName, ScheduleExplorationRequest request) {

        // Check that the NominalComposition exists and that its directory exists
        NominalComposition nominalComposition = compositionRepo.findByName(nominalCompositionName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nominal Composition not found"));

        String nominalCompositionDir = fileSystem.join(DATA_ROOT, nominalCompositionName);

        if (!fileSystem.pathExists(nominalCompositionDir)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(
                    "Directory for Nominal Composition '%s' not found",
                    nominalCompositionName
            ));
        }

        // Checking the consistency of directories for all requested runs
        int nextRunNumber = runRepo.findMaxRunNumberByNominalCompositionId(nominalComposition.getId()).orElse(0) + 1;
        for (int runNumber = nextRunNumber; runNumber < nextRunNumber + request.getNumSimulations(); runNumber++) {

            // TODO: handle gaps in the sequence of ID_RUN directories.
            String runDir = fileSystem.join(nominalCompositionDir, "c/md/lammps/100", String.valueOf(nextRunNumber));
            String subRunDir = fileSystem.join(runDir, "2000/0");

            if (!fileSystem.pathExists(runDir) || !fileSystem.pathExists(subRunDir)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(
                        "Directory for ID_RUN '%s' or for SUB_RUN '0' not found for Nominal Composition '%s'",
                        runNumber, nominalCompositionName
                ));
            }

        }

        // Since everything is OK with the folders, persisting the Runs to DB
        List<Run> runs = new ArrayList<>();
        for (int runNumber = nextRunNumber; runNumber < nextRunNumber + request.getNumSimulations(); runNumber++) {

            Run run = Run.builder()
                    .nominalComposition(nominalComposition)
                    .runNumber(runNumber)
                    .status("SCHEDULED") // TODO: constants for statuses
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            SubRun subRun = SubRun.builder()
                    .run(run)
                    .subRunNumber(0)
                    .status("SCHEDULED") // TODO: constants for statuses
                    .scheduledAt(Instant.now())
                    .build();

            run.setSubRuns(List.of(subRun));

            runs.add(run);

        }

        // Returning only DTOs
        return NominalCompositionDto.fromScheduleExploreExploitRequest(nominalComposition, runs);

    }

    /*
     * Class helpers.
     */

    private List<SimulationArtifact> detectSimulationArtifacts(SubRun subRun, String dirPath) {
        List<SimulationArtifact> artifacts = new ArrayList<>();
        try (DirectoryStream<Path> stream = fileSystem.listFiles(dirPath)) {
            for (Path path : stream) {
                if (!fileSystem.isRegularFile(path.toString())) continue;

                String fileName = path.getFileName().toString();
                String artifactType = classifyArtifact(fileName);

                SimulationArtifact artifact = SimulationArtifact.builder()
                        .subRun(subRun)
                        .artifactType(artifactType)
                        .filePath(path.toString())
                        .fileSize((int) fileSystem.getFileSize(path.toString()))
                        .checksum(fileSystem.calculateSHA256(path.toString()))
                        .createdAt(Instant.now())
                        .build();

                artifacts.add(artifact);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading directory: " + dirPath);
        }
        return artifacts;
    }

    private String classifyArtifact(String filename) {
        if (filename.endsWith(".vasp")) return "POSCAR";
        if (filename.endsWith(".out")) return "OUTPUT";
        if (filename.endsWith(".lobster")) return "LOBSTER";
        if (filename.endsWith(".xyz")) return "XYZ";
        return "UNKNOWN";
    }

}
