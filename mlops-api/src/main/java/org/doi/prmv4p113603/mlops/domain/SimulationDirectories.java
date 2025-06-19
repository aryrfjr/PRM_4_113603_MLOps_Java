package org.doi.prmv4p113603.mlops.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.doi.prmv4p113603.mlops.config.MinioProperties;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExploitationRequest;
import org.doi.prmv4p113603.mlops.exception.SimulationDirectoryNotFoundException;
import org.doi.prmv4p113603.mlops.util.FileSystemUtils;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class SimulationDirectories {

    /*
     * NOTE: with @RequiredArgsConstructor, the private final will be expected in the constructor
     */
    private final SimulationType simulationType;
    private final String nominalCompositionName;
    private final String dataRoot;
    private final MinioProperties minioProperties;
    private final S3Client s3Client;
    private int exploreNextRunNumber = -1;
    private int exploreNumSimulations = -1;
    private List<ScheduleExploitationRequest.RunInput> exploitRuns;
    private SimulationDirectory nominalCompositionDir;

    public SimulationDirectory getNominalCompositionDir() {

        if (nominalCompositionDir == null) {
            throw new IllegalStateException("The method load() must be called before use.");
        }

        return nominalCompositionDir;

    }

    /**
     * Checks integrity and loads real input files (read-only from local HD).
     */
    public void load() {

        String nominalCompositionDirName = FileSystemUtils.join(dataRoot, nominalCompositionName);

        if (!FileSystemUtils.pathExists(nominalCompositionDirName) ||
                !FileSystemUtils.pathExists(nominalCompositionDirName + "-SOAPS")) {
            throw new SimulationDirectoryNotFoundException("Directory not found: " + nominalCompositionDirName + "(-SOAPS)");
        } else {

            nominalCompositionDir = new SimulationDirectory(
                    nominalCompositionDirName,
                    SimulationArtifactScope.NOMINAL_COMPOSITION);

            if (simulationType.isExploration()) {

                if (exploreNextRunNumber == -1 || exploreNumSimulations == -1) {
                    throw new IllegalStateException("nextRunNumber and numSimulations not set.");
                }

                // Checking the consistency of directories for all requested Runs and SubRuns
                for (int runNumber = exploreNextRunNumber; runNumber < exploreNextRunNumber + exploreNumSimulations; runNumber++) {

                    // TODO: handle eventual gaps in the sequence of ID_RUN directories.
                    String runDirName = FileSystemUtils.join(nominalCompositionDirName, "c/md/lammps/100", String.valueOf(runNumber));
                    String subRunDirName = FileSystemUtils.join(runDirName, "2000/0");

                    // TODO: refactor for exploitation DataOps service method
                    if (!FileSystemUtils.pathExists(runDirName) || !FileSystemUtils.pathExists(subRunDirName)) {
                        throw new SimulationDirectoryNotFoundException("Directory for ID_RUN '" + runNumber +
                                "' or for SUB_RUN '0' not found for Nominal Composition '" + nominalCompositionName + "'");
                    } else {

                        SimulationDirectory runDir = new SimulationDirectory(runDirName, SimulationArtifactScope.RUN, runNumber);

                        SimulationDirectory subRunDir = new SimulationDirectory(subRunDirName, SimulationArtifactScope.SUB_RUN, 0);

                        runDir.addChild(subRunDir);

                        nominalCompositionDir.addChild(runDir);

                    }

                }

            } else if (simulationType.isExploitation()) {

                if (exploitRuns == null) {
                    throw new IllegalStateException("exploitRuns not set.");
                }

                for (ScheduleExploitationRequest.RunInput runInput : exploitRuns) {

                    String runDirName = FileSystemUtils.join(nominalCompositionDirName, "c/md/lammps/100", String.valueOf(runInput.getRunNumber()));

                    if (!FileSystemUtils.pathExists(runDirName)) {
                        throw new SimulationDirectoryNotFoundException("Directory for ID_RUN '" + runInput.getRunNumber() +
                                "' not found for Nominal Composition '" + nominalCompositionName + "'");
                    }

                    SimulationDirectory runDir = new SimulationDirectory(runDirName,
                            SimulationArtifactScope.RUN, runInput.getRunNumber());

                    for (Integer subRunId : runInput.getSubRuns()) {

                        String subRunDirName = FileSystemUtils.join(runDirName, "2000/", String.valueOf(subRunId));

                        if (!FileSystemUtils.pathExists(subRunDirName)) {
                            throw new SimulationDirectoryNotFoundException("Directory for SUB_RUN '" + runInput.getRunNumber() +
                                    "' not found for Nominal Composition '" + nominalCompositionName + "'");
                        }

                        SimulationDirectory subRunDir = new SimulationDirectory(subRunDirName,
                                SimulationArtifactScope.SUB_RUN, subRunId);

                        runDir.addChild(subRunDir);

                    }

                    nominalCompositionDir.addChild(runDir);

                }

            }

        }

    }

    /**
     * Uploads the loaded files to MinIO.
     */
    public void upload() {

        if (nominalCompositionDir == null) {
            throw new IllegalStateException("The method load() must be called before use.");
        }

    }

}
