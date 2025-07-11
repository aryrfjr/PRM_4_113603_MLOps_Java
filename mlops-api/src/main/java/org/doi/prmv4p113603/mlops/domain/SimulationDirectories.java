package org.doi.prmv4p113603.mlops.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExploitationRequest;
import org.doi.prmv4p113603.mlops.exception.DataOpsInternalInconsistencyException;
import org.doi.prmv4p113603.mlops.exception.SimulationDirectoryNotFoundException;
import org.doi.prmv4p113603.mlops.util.FileSystemUtils;

import java.util.List;

/**
 * This class contains methods to manage different types of simulation directories.
 * It is not a singleton Spring bean; instead, it was designed to be instantiated per
 * method call in DataOps service with different internal state (different constructor
 * args and setters). Its lifecycle is scoped to different methods in DataOps service,
 * i.e., it’s a short-lived, stateful helper.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class SimulationDirectories {

    /*
     * NOTE: This class should be annotated with @Component only if having
     *  Spring managing its lifecycle and dependencies was needed. However,
     *  this class is instantiated manually inside its factory, and it is not
     *  a singleton and carries per-call state.
     *
     * NOTE: with @RequiredArgsConstructor, the private final will be expected in the constructor
     */

    private final SimulationType simulationType;
    private final String nominalCompositionName;
    private final String dataRoot;
    private SimulationDirectory nominalCompositionDir;
    private int exploreNextRunNumber = -1;
    private int exploreNumSimulations = -1;
    private List<ScheduleExploitationRequest.RunInput> exploitRuns;
    private int etlRunNumber = -1;
    private int etlSubRunNumber = -1;

    public SimulationDirectory getNominalCompositionDir() {

        if (nominalCompositionDir == null) {
            throw new DataOpsInternalInconsistencyException(
                    "The method load() must be called before the method getNominalCompositionDir().");
        }

        return nominalCompositionDir;

    }

    /**
     * Checks integrity and loads real input files (read-only from local HD).
     */
    public void load() {

        String nominalCompositionDirName = FileSystemUtils.join(dataRoot, nominalCompositionName);

        if ((simulationType.isGenerateExploration() || simulationType.isGenerateExploitation())
                && !FileSystemUtils.pathExists(nominalCompositionDirName)) {
            throw new SimulationDirectoryNotFoundException(nominalCompositionDirName);
        } else if (simulationType.isEtl() && !FileSystemUtils.pathExists(nominalCompositionDirName + "-SOAPS")) {
            throw new SimulationDirectoryNotFoundException(nominalCompositionDirName + "-SOAPS");
        } else {

            if (simulationType.isGenerateExploration() || simulationType.isGenerateExploitation()) {
                nominalCompositionDir = new SimulationDirectory(
                        nominalCompositionDirName,
                        SimulationArtifactScope.NOMINAL_COMPOSITION);
            } else if (simulationType.isEtl()) {
                nominalCompositionDir = new SimulationDirectory(
                        nominalCompositionDirName + "-SOAPS",
                        SimulationArtifactScope.NOMINAL_COMPOSITION);
            }

            if (simulationType.isGenerateExploration()) {

                if (exploreNextRunNumber == -1 || exploreNumSimulations == -1) {
                    throw new DataOpsInternalInconsistencyException("Attributes nextRunNumber and numSimulations not set.");
                }

                // Checking the consistency of directories for all requested Runs and SubRuns
                for (int runNumber = exploreNextRunNumber; runNumber < exploreNextRunNumber + exploreNumSimulations; runNumber++) {

                    // TODO: handle eventual gaps in the sequence of ID_RUN directories.
                    String runDirName = FileSystemUtils.join(nominalCompositionDirName, "c/md/lammps/100", String.valueOf(runNumber));
                    String subRunDirName = FileSystemUtils.join(runDirName, "2000/0");

                    if (!FileSystemUtils.pathExists(runDirName)) {
                        throw new SimulationDirectoryNotFoundException(runDirName);
                    } else if (!FileSystemUtils.pathExists(subRunDirName)) {
                        throw new SimulationDirectoryNotFoundException(subRunDirName);
                    } else {

                        SimulationDirectory runDir = new SimulationDirectory(runDirName, SimulationArtifactScope.RUN, runNumber);

                        SimulationDirectory subRunDir = new SimulationDirectory(subRunDirName, SimulationArtifactScope.SUB_RUN, 0);

                        runDir.addChild(subRunDir);

                        nominalCompositionDir.addChild(runDir);

                    }

                }

            } else if (simulationType.isGenerateExploitation()) {

                if (exploitRuns == null) {
                    throw new DataOpsInternalInconsistencyException("Attribute exploitRuns not set.");
                }

                for (ScheduleExploitationRequest.RunInput runInput : exploitRuns) {

                    String runDirName = FileSystemUtils.join(nominalCompositionDirName, "c/md/lammps/100", String.valueOf(runInput.getRunNumber()));

                    if (!FileSystemUtils.pathExists(runDirName)) {
                        throw new SimulationDirectoryNotFoundException(runDirName);
                    }

                    SimulationDirectory runDir = new SimulationDirectory(runDirName,
                            SimulationArtifactScope.RUN, runInput.getRunNumber());

                    for (Integer subRunNumber : runInput.getSubRuns()) {

                        String subRunDirName = FileSystemUtils.join(runDirName, "2000/", String.valueOf(subRunNumber));

                        if (!FileSystemUtils.pathExists(subRunDirName)) {
                            throw new SimulationDirectoryNotFoundException(subRunDirName);
                        }

                        SimulationDirectory subRunDir = new SimulationDirectory(subRunDirName,
                                SimulationArtifactScope.SUB_RUN, subRunNumber);

                        runDir.addChild(subRunDir);

                    }

                    nominalCompositionDir.addChild(runDir);

                }

            } else if (simulationType.isEtl()) {

                if (etlRunNumber == -1 || etlSubRunNumber == -1) {
                    throw new DataOpsInternalInconsistencyException("Attributes etlRunNumber and/or etlSubRunNumber not set.");
                }

                String runDirName = FileSystemUtils.join(nominalCompositionDirName, "c/md/lammps/100", String.valueOf(etlRunNumber));

                if (!FileSystemUtils.pathExists(runDirName)) {
                    throw new SimulationDirectoryNotFoundException(runDirName);
                }

                SimulationDirectory runDir = new SimulationDirectory(runDirName, SimulationArtifactScope.RUN, etlRunNumber);

                String subRunDirName = FileSystemUtils.join(runDirName, "2000/", String.valueOf(etlSubRunNumber));

                if (!FileSystemUtils.pathExists(subRunDirName)) {
                    throw new SimulationDirectoryNotFoundException(subRunDirName);
                }

                SimulationDirectory subRunDir = new SimulationDirectory(subRunDirName,
                        SimulationArtifactScope.SUB_RUN, etlSubRunNumber);

                runDir.addChild(subRunDir);

                nominalCompositionDir.addChild(runDir);

            }

        }

    }

}
