package org.doi.prmv4p113603.mlops.service;

import org.doi.prmv4p113603.mlops.data.dto.*;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExplorationRequest;
import org.doi.prmv4p113603.mlops.model.*;
import org.doi.prmv4p113603.mlops.repository.*;
import org.doi.prmv4p113603.mlops.util.FileSystemUtils;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
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
public class DataOpsService {

    private final NominalCompositionRepository compositionRepo;
    private final RunRepository runRepo;

    public DataOpsService(NominalCompositionRepository compositionRepo, RunRepository runRepo) {
        this.compositionRepo = compositionRepo;
        this.runRepo = runRepo;
    }

    public RunDto scheduleExploration(String compositionName, ScheduleExplorationRequest request) {
        NominalComposition nc = compositionRepo.findByName(compositionName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nominal Composition not found"));

        int nextRunNumber = runRepo.findMaxRunNumberByNominalCompositionId(nc.getId()).orElse(0) + 1;

        Run run = Run.builder()
                .nominalComposition(nc)
                .runNumber(nextRunNumber)
                .status("SCHEDULED")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        List<SubRun> subRuns = new ArrayList<>();

        for (int i = 1; i <= request.getNumSimulations(); i++) {
            String subRunDir = FileSystemUtils.join("/data", compositionName, "run_" + nextRunNumber, "sub_run_" + i);
            if (!FileSystemUtils.pathExists(subRunDir)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SubRun directory not found: " + subRunDir);
            }

            SubRun subRun = SubRun.builder()
                    .run(run)
                    .subRunNumber(i)
                    .status("SCHEDULED")
                    .scheduledAt(Instant.now())
                    .build();

            List<SimulationArtifact> artifacts = detectSimulationArtifacts(subRun, subRunDir);
            subRun.setSimulationArtifacts(artifacts);
            subRuns.add(subRun);
        }

        run.setSubRuns(subRuns);
        runRepo.save(run);

        return RunDto.builder()
                .nominalCompositionId(nc.getId())
                .runNumber(run.getRunNumber())
                .status(run.getStatus())
                .subRuns(subRuns.stream()
                        .map(sr -> SubRunDto.builder()
                                .subRunNumber(sr.getSubRunNumber())
                                .status(sr.getStatus())
                                .simulationArtifacts(sr.getSimulationArtifacts().stream()
                                        .map(a -> SimulationArtifactDto.builder()
                                                .artifactType(a.getArtifactType())
                                                .filePath(a.getFilePath())
                                                .fileSize(a.getFileSize() != null ? a.getFileSize().longValue() : null)
                                                .checksum(a.getChecksum())
                                                .createdAt(a.getCreatedAt())
                                                .build())
                                        .collect(Collectors.toList()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private List<SimulationArtifact> detectSimulationArtifacts(SubRun subRun, String dirPath) {
        List<SimulationArtifact> artifacts = new ArrayList<>();
        try (DirectoryStream<Path> stream = FileSystemUtils.listFiles(dirPath)) {
            for (Path path : stream) {
                if (!FileSystemUtils.isRegularFile(path.toString())) continue;

                String fileName = path.getFileName().toString();
                String artifactType = classifyArtifact(fileName);

                SimulationArtifact artifact = SimulationArtifact.builder()
                        .subRun(subRun)
                        .artifactType(artifactType)
                        .filePath(path.toString())
                        .fileSize((int) FileSystemUtils.getFileSize(path.toString()))
                        .checksum(FileSystemUtils.calculateSHA256(path.toString()))
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
