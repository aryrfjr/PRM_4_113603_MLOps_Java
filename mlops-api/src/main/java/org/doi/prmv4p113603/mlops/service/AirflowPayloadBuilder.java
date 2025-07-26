package org.doi.prmv4p113603.mlops.service;

import org.doi.prmv4p113603.mlops.data.request.AirflowDagRunRequest;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.doi.prmv4p113603.mlops.model.Run;
import org.doi.prmv4p113603.mlops.model.SimulationArtifact;
import org.doi.prmv4p113603.mlops.model.SubRun;
import org.doi.prmv4p113603.mlops.exception.SimulationArtifactNotFoundException;
import org.doi.prmv4p113603.mlops.domain.SimulationArtifactType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AirflowPayloadBuilder {

    public AirflowDagRunRequest buildExplorationRequest(NominalComposition composition, List<Run> runs) {
        String compositionName = composition.getName();

        List<AirflowDagRunRequest.RunJob> runJobs = runs.stream()
                .flatMap(run -> run.getSubRuns().stream())
                .filter(subRun -> subRun.getSubRunNumber() == 0)
                .map(this::buildRunJob)
                .toList();

        List<AirflowDagRunRequest.RunWithSubRuns> allRunsWithSubRuns = runs.stream()
                .map(run -> AirflowDagRunRequest.RunWithSubRuns.builder()
                        .runNumber(run.getRunNumber())
                        .subRunsNumbers(run.getSubRuns().stream()
                                .map(SubRun::getSubRunNumber)
                                .sorted()
                                .toList())
                        .build())
                .toList();

        // Example: you might want to load this from application.yml later
        AirflowDagRunRequest.SoapParameters soapParams = AirflowDagRunRequest.SoapParameters.builder()
                .cutoff(3.75)
                .lMax(6)
                .nMax(8)
                .nZ(3)
                .z("{13 29 40}")
                .nSpecies(3)
                .ZSpecies("{13 29 40}")
                .build();

        return AirflowDagRunRequest.buildFrom(
                compositionName,
                runJobs,
                allRunsWithSubRuns,
                soapParams,
                "string" // TODO: Replace with user input or config
        );
    }

    private AirflowDagRunRequest.RunJob buildRunJob(SubRun subRun) {
        return AirflowDagRunRequest.RunJob.builder()
                .runNumber(subRun.getRun().getRunNumber())
                .jobs(List.of(
                        buildJob(subRun, SimulationArtifactType.LAMMPS_INPUT, Set.of(
                                SimulationArtifactType.LAMMPS_DUMP,
                                SimulationArtifactType.LAMMPS_LOG,
                                SimulationArtifactType.LAMMPS_OUTPUT
                        )),
                        buildJob(subRun, SimulationArtifactType.QE_SCF_IN, Set.of(
                                SimulationArtifactType.QE_SCF_OUT,
                                SimulationArtifactType.LAMMPS_DUMP_XYZ
                        )),
                        buildJob(subRun, SimulationArtifactType.LOBSTER_INPUT, Set.of(
                                SimulationArtifactType.LOBSTER_OUTPUT,
                                SimulationArtifactType.LOBSTER_RUN_OUTPUT,
                                SimulationArtifactType.ICOHPLIST
                        ))
                ))
                .build();
    }

    private AirflowDagRunRequest.Job buildJob(SubRun subRun,
                                              SimulationArtifactType inputType,
                                              Set<SimulationArtifactType> outputTypes) {

        SimulationArtifact input = subRun.getSimulationArtifacts().stream()
                .filter(a -> a.getArtifactType() == inputType)
                .findFirst()
                .orElseThrow(() -> new SimulationArtifactNotFoundException(inputType.name()));

        Set<String> outputPaths = subRun.getSimulationArtifacts().stream()
                .filter(a -> outputTypes.contains(a.getArtifactType()))
                .map(SimulationArtifact::getFilePath)
                .collect(Collectors.toSet());

        return AirflowDagRunRequest.Job.builder()
                .inputFile(input.getFilePath())
                .outputFiles(outputPaths.stream().toList())
                .build();
    }
}
