package org.doi.prmv4p113603.mlops.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.doi.prmv4p113603.mlops.exception.SimulationDirectoryNotFoundException;
import org.doi.prmv4p113603.mlops.util.FileSystemUtils;

@Getter
@Setter
@RequiredArgsConstructor
public class SimulationDirectories {

    /*
     * NOTE: with @RequiredArgsConstructor, the private final will be expected in the constructor
     */
    private final String nominalCompositionName;
    private final String dataRoot;
    private int nextRunNumber = -1;
    private int numSimulations = -1;
    private SimulationDirectory nominalCompositionDir;

    public void load(boolean runExploration) {

        String nominalCompositionDirName = FileSystemUtils.join(dataRoot, nominalCompositionName);

        if (!FileSystemUtils.pathExists(nominalCompositionDirName) ||
                !FileSystemUtils.pathExists(nominalCompositionDirName + "-SOAPS")) {
            throw new SimulationDirectoryNotFoundException("Directory not found: " + nominalCompositionDirName + "(-SOAPS)");
        } else {

            nominalCompositionDir = new SimulationDirectory(
                    nominalCompositionDirName,
                    SimulationArtifactScope.NOMINAL_COMPOSITION, -1);

            if (runExploration) { // called from exploration DataOps task

                if (nextRunNumber == -1 || numSimulations == -1) {
                    throw new SimulationDirectoryNotFoundException("nextRunNumber and numSimulations not set.");
                }

                // Checking the consistency of directories for all requested Runs and SubRuns
                for (int runNumber = nextRunNumber; runNumber < nextRunNumber + numSimulations; runNumber++) {

                    // TODO: handle gaps in the sequence of ID_RUN directories.
                    String runDirName = FileSystemUtils.join(nominalCompositionDirName, "c/md/lammps/100", String.valueOf(nextRunNumber));
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

            } // TODO: called from exploitation DataOps task (generalize?)

        }

    }

}
