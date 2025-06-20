package org.doi.prmv4p113603.mlops.domain;

import lombok.RequiredArgsConstructor;
import org.doi.prmv4p113603.mlops.config.MlopsProperties;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExploitationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Spring-managed factory for SimulationDirectories instances.
 */
@Component
@RequiredArgsConstructor
public class SimulationDirectoriesFactory {

    // Dependencies
    private final MlopsProperties mlopsProperties;

    public SimulationDirectories create(
            SimulationType simulationType,
            String nominalCompositionName,
            int exploreNextRunNumber,
            int exploreNumSimulations) {

        SimulationDirectories simulationDirectories = new SimulationDirectories(
                simulationType,
                nominalCompositionName,
                mlopsProperties.getDataRoot());

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
                mlopsProperties.getDataRoot());

        simulationDirectories.setExploitRuns(exploitRuns);

        return simulationDirectories;

    }

}
