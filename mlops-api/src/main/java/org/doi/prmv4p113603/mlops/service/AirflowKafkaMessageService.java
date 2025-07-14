package org.doi.prmv4p113603.mlops.service;

import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.config.MlopsProperties;
import org.doi.prmv4p113603.mlops.data.dto.messaging.AirflowKafkaMessageDto;
import org.doi.prmv4p113603.mlops.domain.*;
import org.doi.prmv4p113603.mlops.exception.*;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Contains business logic for handling different message types.
 * <p>
 * TODO: could be the implementation of an interface MessageService.
 */
@Service
@AllArgsConstructor
public class AirflowKafkaMessageService {

    private final NominalCompositionRepository nominalCompositionRepository;
    private final RunRepository runRepository;
    private final SubRunRepository subRunRepository;
    private final SimulationArtifactRepository simulationArtifactRepository;
    private final MlopsProperties mlopsProperties;

    @Transactional
    public void process(AirflowKafkaMessageDto message) {

        System.out.println("Received message: " + message);

        NominalComposition nominalComposition = nominalCompositionRepository.findByName(message.getNominalComposition())
                .orElseThrow(() -> new NominalCompositionNotFoundException(message.getNominalComposition()));

        Run run = runRepository.findByNominalCompositionAndRunNumber(nominalComposition, message.getRunNumber())
                .orElseThrow(() -> new RunNotFoundException(String.valueOf(message.getRunNumber())));

        if (message.getType().isRunSubmitted()) {
            run.setStatus(RunStatus.EXPLORATION_RUNNING);
        } else if (message.getType().isRunSubmissionFailed()) {
            run.setStatus(RunStatus.EXPLORATION_FAILED);
        } else if (message.getType().isSsdbCreated()) {
            run.setStatus(RunStatus.ETL_COMPLETED);
        } else if (message.getType().isSoapVectorsExtractionFailed() || message.getType().isSsdbCreationFailed()) {
            run.setStatus(RunStatus.ETL_FAILED);
        } else if (message.getType().isSoapVectorsExtracted()) {

            for (int subRunNumber : message.getSubRunNumbers()) {

                run.setStatus(RunStatus.ETL_COMPLETED);

                SimulationArtifact sa = createSoapVectorsArtifacts(nominalComposition, run, subRunNumber);

                System.out.println("Simulation Artifact for SOAP vectors created: " + sa);

            }

        }

    }

    private SimulationArtifact createSoapVectorsArtifacts(NominalComposition nominalComposition, Run run, int subRunNumber) {

        SubRun subRun = subRunRepository.findByRunAndSubRunNumber(run, subRunNumber)
                .orElseThrow(() -> new SubRunNotFoundException("Run not found"));

        SimulationDirectories simulationDirectories = new SimulationDirectories(
                SimulationType.ETL,
                nominalComposition.getName(),
                mlopsProperties.getDataRoot());

        simulationDirectories.setEtlRunNumber(run.getRunNumber());
        simulationDirectories.setEtlSubRunNumber(subRun.getSubRunNumber());

        simulationDirectories.load();

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
