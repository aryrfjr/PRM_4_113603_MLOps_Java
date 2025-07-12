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
        if (message.getType().isSsdbCreated()) {
            System.out.println("Received message: " + message.getExternalPipelineRunId());
        } else if (message.getType().isSoapVectorsExtracted()) {

            System.out.println("Received message: " + message.getExternalPipelineRunId());



        }
    }

    private SimulationArtifact createSoapVectorsArtifact(String ncName, int runNumber, int subRunNumber,
                                             String filePath,
                                             Integer fileSize,
                                             String checksum) {

        NominalComposition nc = nominalCompositionRepository.findByName(ncName)
                .orElseThrow(() -> new NominalCompositionNotFoundException(ncName));

        Run run = runRepository.findByNominalCompositionAndRunNumber(nc, runNumber)
                .orElseThrow(() -> new RunNotFoundException("Run not found"));

        SubRun subRun = subRunRepository.findByRunAndSubRunNumber(run, subRunNumber)
                .orElseThrow(() -> new SubRunNotFoundException("SubRun not found"));

        SimulationDirectories simulationDirectories = new SimulationDirectories(
                SimulationType.ETL,
                nc.getName(),
                mlopsProperties.getDataRoot());

        simulationDirectories.setEtlRunNumber(run.getRunNumber());
        simulationDirectories.setEtlSubRunNumber(subRun.getSubRunNumber());

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

        return simulationArtifactRepository.save(artifact);

    }

}
