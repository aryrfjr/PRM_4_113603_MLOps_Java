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
import org.doi.prmv4p113603.mlops.repository.SimulationArtifactRepository;
import org.doi.prmv4p113603.mlops.repository.SubRunRepository;
import org.doi.prmv4p113603.mlops.util.ExpectedSimulationArtifacts;
import org.doi.prmv4p113603.mlops.util.SimulationArtifactFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@AllArgsConstructor
public class SoapVectorsExtractedMessageHandler implements MessageHandler {

    private final RunRepository runRepository;
    private final NominalCompositionRepository nominalCompositionRepository;
    private final SubRunRepository subRunRepository;
    private final SimulationArtifactRepository simulationArtifactRepository;
    private final MlopsProperties mlopsProperties;

    @Override
    public boolean canHandle(MessageDto message) {
        return message.getType().isSoapVectorsExtracted();
    }

    @Transactional
    public void handle(MessageDto message) {

        NominalComposition nominalComposition = nominalCompositionRepository.findByName(message.getNominalComposition())
                .orElseThrow(() -> new NominalCompositionNotFoundException(message.getNominalComposition()));

        Run run = runRepository.findByNominalCompositionAndRunNumber(nominalComposition, message.getRunNumber())
                .orElseThrow(() -> new RunNotFoundException(String.valueOf(message.getRunNumber())));

        for (int subRunNumber : message.getSubRunNumbers()) {

            run.setStatus(RunStatus.ETL_COMPLETED);

            SubRun subRun = subRunRepository.findByRunAndSubRunNumber(run, subRunNumber)
                    .orElseThrow(() -> new SubRunNotFoundException(String.valueOf(subRunNumber)));

            SimulationArtifact sa = createSoapVectorsSimulationArtifact(nominalComposition, run, subRun);

            subRun.setExternalPipelineRunId(message.getExternalPipelineRunId());

            System.out.println("Simulation Artifact for SOAP vectors created: " + sa);

        }

    }

    /*
     * Helpers.
     */

    private SimulationArtifact createSoapVectorsSimulationArtifact(NominalComposition nominalComposition, Run run, SubRun subRun) {

        SimulationDirectories simulationDirectories = new SimulationDirectories(
                SimulationType.ETL,
                nominalComposition.getName(),
                mlopsProperties.getDataRoot());

        simulationDirectories.setEtlRunNumber(run.getRunNumber());
        simulationDirectories.setEtlSubRunNumber(subRun.getSubRunNumber());

        simulationDirectories.load();

        // TODO: that part must be generalized for other SubRuns when messaged by Pre-Deployment Exploitation
        SimulationDirectory subRunSimDir = simulationDirectories
                .getNominalCompositionDir().getChildren().get(0).getChildren().get(0);

        String soapFileName = ExpectedSimulationArtifacts.getFileNamePatternForType(SimulationArtifactType.SOAP_VECTORS);

        Path soapFilePath = Path.of(subRunSimDir.getPath()).resolve(soapFileName);

        if (!Files.exists(soapFilePath)) {
            throw new SimulationArtifactNotFoundException(soapFilePath.toAbsolutePath().toString());
        }

        SimulationArtifact artifact = SimulationArtifact.builder()
                .subRun(subRun)
                .artifactType(SimulationArtifactType.SOAP_VECTORS)
                .artifactRole(SimulationArtifactRole.ETL_OUTPUT)
                .filePath(soapFilePath.toString())
                .fileSize(SimulationArtifactFactory.getFileSize(soapFilePath))
                .checksum(SimulationArtifactFactory.computeChecksum(soapFilePath))
                .build();

        // NOTE: When creating new entities within a @Transactional context, calling .save() is required.
        return simulationArtifactRepository.save(artifact);

    }

}
