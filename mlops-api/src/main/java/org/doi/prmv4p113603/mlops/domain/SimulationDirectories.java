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
    private int exploreRunNumber = -1;
    private int exploreSubRunNumber = -1;
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
    // TODO: Apply handler mapping here like I did for AirflowMessageService.
    public void load() {

        String nominalCompositionDirName = null;
        if (simulationType.isGenerateExploration() || simulationType.isGenerateExploitation()) {
            nominalCompositionDirName = FileSystemUtils.join(dataRoot, nominalCompositionName);
        } else if (simulationType.isEtl()) {
            nominalCompositionDirName = FileSystemUtils.join(dataRoot, nominalCompositionName + "-SOAPS");
        }

        if (nominalCompositionDirName == null || !FileSystemUtils.pathExists(nominalCompositionDirName)) {
            throw new SimulationDirectoryNotFoundException(nominalCompositionDirName);
        } else {

            nominalCompositionDir = new SimulationDirectory(
                    nominalCompositionDirName,
                    SimulationArtifactScope.NOMINAL_COMPOSITION);

            if (simulationType.isGenerateExploration()) {

                if (exploreNextRunNumber != -1 && exploreNumSimulations != -1) {

                    // Checking the consistency of directories for all requested Runs and SubRuns
                    for (int runNumber = exploreNextRunNumber; runNumber < exploreNextRunNumber + exploreNumSimulations; runNumber++) {
                        loadForRunAndSubRun(nominalCompositionDirName, runNumber, 0);
                    }

                } else if (exploreRunNumber != -1 && exploreSubRunNumber != -1) {
                    loadForRunAndSubRun(nominalCompositionDirName, exploreRunNumber, exploreSubRunNumber);
                } else {
                    throw new DataOpsInternalInconsistencyException("Attributes nextRunNumber and numSimulations, or exploreRunNumber and exploreSubRunNumber must be set.");
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

                loadForRunAndSubRun(nominalCompositionDirName, etlRunNumber, etlSubRunNumber);

            }

        }

    }

    /*
     * Helpers.
     */

    private void loadForRunAndSubRun(String nominalCompositionDirName, int runNumberToLoad, int subRunNumerToLoad) {

        String runDirName = FileSystemUtils.join(nominalCompositionDirName, "c/md/lammps/100", String.valueOf(runNumberToLoad));

        if (!FileSystemUtils.pathExists(runDirName)) {
            throw new SimulationDirectoryNotFoundException(runDirName);
        }

        SimulationDirectory runDir = new SimulationDirectory(runDirName, SimulationArtifactScope.RUN, runNumberToLoad);

        String subRunDirName = FileSystemUtils.join(runDirName, "2000/", String.valueOf(subRunNumerToLoad));

        if (!FileSystemUtils.pathExists(subRunDirName)) {
            throw new SimulationDirectoryNotFoundException(subRunDirName);
        }

        SimulationDirectory subRunDir = new SimulationDirectory(subRunDirName,
                SimulationArtifactScope.SUB_RUN, subRunNumerToLoad);

        runDir.addChild(subRunDir);

        nominalCompositionDir.addChild(runDir);

    }

}
