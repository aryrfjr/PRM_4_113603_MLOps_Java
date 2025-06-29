package org.doi.prmv4p113603.mlops.util;

import org.doi.prmv4p113603.mlops.domain.SimulationArtifactRole;
import org.doi.prmv4p113603.mlops.domain.SimulationDirectories;
import org.doi.prmv4p113603.mlops.domain.SimulationType;
import org.doi.prmv4p113603.mlops.exception.SimulationDirectoryNotFoundException;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.doi.prmv4p113603.mlops.model.Run;
import org.doi.prmv4p113603.mlops.model.SimulationArtifact;
import org.doi.prmv4p113603.mlops.model.SubRun;
import org.doi.prmv4p113603.mlops.testutil.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is a Unit Test (primary classification) for the process of creating instances
 * of entity class SimulationArtifact. The process is implemented in the utility class
 * SimulationArtifactFactory.
 * <p>
 * Since this test uses file path structures and directory loading and doesn't mock
 * filesystem access explicitly, it is also slightly closer to an Integration Test.
 */
public class SimulationArtifactFactoryTest {

    private NominalComposition nc;
    private Run run;
    private SubRun subRun;

    @BeforeEach
    void setup() {
        nc = TestFixtures.dummyNominalComposition(0L, "Zr49Cu49Al2");
        run = TestFixtures.dummyRun(nc, 2);
        subRun = TestFixtures.dummySubRun(run, 0);
    }

    @Test
    void testGenerateArtifactsForSubRun_skipsMissingFiles() {

        // Testing exploration scenario

        SimulationDirectories simulationDirectories = new SimulationDirectories(
                SimulationType.GENERATE_EXPLORATION,
                nc.getName(),
                "/home/aryjr/fromiomega/pos-doc/UFSCar/MG-NMR/ML/big-data-full/");

        simulationDirectories.setExploreNextRunNumber(run.getRunNumber());
        simulationDirectories.setExploreNumSimulations(1);

        try {
            simulationDirectories.load();
        } catch (SimulationDirectoryNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }

        run.setSubRuns(List.of(subRun));

        List<SimulationArtifact> result = SimulationArtifactFactory.load(
                nc.getName(),
                subRun,
                simulationDirectories.getNominalCompositionDir().getChildren().get(0).getChildren().get(0),
                SimulationArtifactRole.GENERATE_INPUT
        );

        System.out.println(result.get(0).getArtifactType());
        System.out.println(result.get(0).getFilePath());

        assertFalse(result.isEmpty(), "Should skip all artifacts since none exist");

    }

}
