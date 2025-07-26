package org.doi.prmv4p113603.mlops.service;

import lombok.RequiredArgsConstructor;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExploitationRequest;
import org.doi.prmv4p113603.mlops.domain.SimulationDirectory;
import org.doi.prmv4p113603.mlops.domain.SimulationDirectories;
import org.doi.prmv4p113603.mlops.domain.SimulationDirectoriesFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SimulationDirectoryLoader {

    private final SimulationDirectoriesFactory factory;

    public List<SimulationDirectory> loadExplorationDirs(String name, int runNumberStart, int numSimulations) {
        SimulationDirectories dirs = factory.createForExploration(name, runNumberStart, numSimulations);
        dirs.load();
        return dirs.getNominalCompositionDir().getChildren();
    }

    public List<SimulationDirectory> loadExploitationDirs(String name, List<ScheduleExploitationRequest.RunInput> runs) {
        SimulationDirectories dirs = factory.createForExploitation(name, runs);
        dirs.load();
        return dirs.getNominalCompositionDir().getChildren();
    }

}
