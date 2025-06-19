package org.doi.prmv4p113603.mlops.domain;

import lombok.RequiredArgsConstructor;
import org.doi.prmv4p113603.mlops.config.MinioProperties;
import org.doi.prmv4p113603.mlops.config.MlopsProperties;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExploitationRequest;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;

/**
 * Spring-managed factory for SimulationDirectories instances.
 */
@Component
@RequiredArgsConstructor
public class SimulationDirectoriesFactory {

    // Dependencies
    private final MlopsProperties mlopsProperties;
    private final MinioProperties minioProperties;
    private final S3Client s3Client;

    public SimulationDirectories create(
            SimulationType simulationType,
            String nominalCompositionName,
            int exploreNextRunNumber,
            int exploreNumSimulations) {

        SimulationDirectories simulationDirectories = new SimulationDirectories(
                simulationType,
                nominalCompositionName,
                mlopsProperties.getDataRoot(),
                minioProperties,
                s3Client);

        simulationDirectories.setExploreNextRunNumber(exploreNextRunNumber);
        simulationDirectories.setExploreNumSimulations(exploreNumSimulations);

        return simulationDirectories;

    }

    public SimulationDirectories create(
            SimulationType simulationType,
            String nominalCompositionName,
            List<ScheduleExploitationRequest.RunInput> exploitRuns) {

        SimulationDirectories simulationDirectories = new SimulationDirectories(
                simulationType,
                nominalCompositionName,
                mlopsProperties.getDataRoot(),
                minioProperties,
                s3Client);

        simulationDirectories.setExploitRuns(exploitRuns);

        return simulationDirectories;

    }

}
