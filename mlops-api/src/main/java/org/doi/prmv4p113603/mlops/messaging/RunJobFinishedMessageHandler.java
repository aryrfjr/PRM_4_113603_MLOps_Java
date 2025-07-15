package org.doi.prmv4p113603.mlops.messaging;

import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.config.MlopsProperties;
import org.doi.prmv4p113603.mlops.data.dto.messaging.MessageDto;
import org.doi.prmv4p113603.mlops.domain.*;
import org.doi.prmv4p113603.mlops.exception.NominalCompositionNotFoundException;
import org.doi.prmv4p113603.mlops.exception.RunNotFoundException;
import org.doi.prmv4p113603.mlops.exception.SimulationArtifactNotFoundException;
import org.doi.prmv4p113603.mlops.exception.SubRunNotFoundException;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.doi.prmv4p113603.mlops.model.Run;
import org.doi.prmv4p113603.mlops.model.SimulationArtifact;
import org.doi.prmv4p113603.mlops.model.SubRun;
import org.doi.prmv4p113603.mlops.repository.NominalCompositionRepository;
import org.doi.prmv4p113603.mlops.repository.RunRepository;
import org.doi.prmv4p113603.mlops.repository.SubRunRepository;
import org.doi.prmv4p113603.mlops.util.SimulationArtifactFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Component
@AllArgsConstructor
public class RunJobFinishedMessageHandler implements MessageHandler {

    private final SimulationArtifactPathParser simulationArtifactPathParser;
    private final NominalCompositionRepository nominalCompositionRepository;
    private final RunRepository runRepository;
    private final SubRunRepository subRunRepository;
    private final MlopsProperties mlopsProperties;

    @Override
    public boolean canHandle(MessageDto message) {
        return message.getType().isRunJobFinished();
    }

    @Transactional
    public void handle(MessageDto message) {

        MessageDto.HpcJobInfoDto jobInfo = message.getJobInfo();

        // All output files will be in the same directory
        String pathFirst = jobInfo.getOutputFiles().stream().findFirst()
                .orElseThrow(() -> new SimulationArtifactNotFoundException(message.getExternalPipelineRunId()));

        SimulationArtifactPathParser.SimulationArtifactPathInfo simulationArtifactPathInfo = simulationArtifactPathParser.parse(pathFirst);

        String nominalCompositionName = simulationArtifactPathInfo.nominalCompositionName();
        String runNumber = simulationArtifactPathInfo.runNumber();
        String subRunNumber = Objects.requireNonNullElse(simulationArtifactPathInfo.subRunNumber(), "0");

        NominalComposition nominalComposition = nominalCompositionRepository.findByName(nominalCompositionName)
                .orElseThrow(() -> new NominalCompositionNotFoundException(nominalCompositionName));

        Run run = runRepository.findByNominalCompositionAndRunNumber(nominalComposition, Integer.parseInt(runNumber))
                .orElseThrow(() -> new RunNotFoundException(runNumber));

        SubRun subRun = subRunRepository.findByRunAndSubRunNumber(run, Integer.parseInt(subRunNumber))
                .orElseThrow(() -> new SubRunNotFoundException(subRunNumber));

        List<SimulationArtifact> subRunSimulationArtifacts = subRun.getSimulationArtifacts();
        for (String path : jobInfo.getOutputFiles()) {

            Path outputFilePath = Path.of(path);

            if (!Files.exists(outputFilePath)) {
                throw new SimulationArtifactNotFoundException(outputFilePath.toAbsolutePath().toString());
            }

            SimulationArtifact simulationArtifact = subRunSimulationArtifacts.stream()
                    .filter(a -> a.getFilePath().equals(path))
                    .findFirst().orElse(null);

            if (simulationArtifact == null) {
                throw new SimulationArtifactNotFoundException(path);
            } else {
                simulationArtifact.setFileSize(SimulationArtifactFactory.getFileSize(outputFilePath));
                simulationArtifact.setChecksum(SimulationArtifactFactory.computeChecksum(outputFilePath));
            }

        }

    }

}
