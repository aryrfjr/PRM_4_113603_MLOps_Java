package org.doi.prmv4p113603.mlops.service;

import org.doi.prmv4p113603.mlops.config.MlopsProperties;
import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionDto;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExplorationRequest;
import org.doi.prmv4p113603.mlops.domain.*;
import org.doi.prmv4p113603.mlops.model.*;
import org.doi.prmv4p113603.mlops.repository.NominalCompositionRepository;
import org.doi.prmv4p113603.mlops.repository.RunRepository;
import org.doi.prmv4p113603.mlops.util.SimulationArtifactFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test class for the DataOps service. Dependencies are mocked with Mockito.
 * <p>
 * It is focused on testing the DataOps service in isolation, by stubbing all external
 * dependencies without loading the Spring context. No real database, no real file system
 * access; everything is mocked.
 */
@ExtendWith(MockitoExtension.class)
class DataOpsServiceTest {

    /*
     * This is a mock object (i.e., not a real Spring Data repository — just a dummy
     * that will behave as per instructions provided with the method 'when' below).
     *
     * The same is true for 'runRepo' and 'mlopsProperties'.
     */
    @Mock
    NominalCompositionRepository compositionRepo;

    @Mock
    RunRepository runRepo;

    @Mock
    MlopsProperties mlopsProperties;

    @Test
    void testScheduleExploration_success() throws Exception {

        // TODO: set in a file 'application.properties' for tests
        final String dataRoot = "/home/aryjr/fromiomega/pos-doc/UFSCar/MG-NMR/ML/big-data-full/";

        // Starting Mockito setup
        String nominalCompositionName = "Zr49Cu49Al2";
        long nominalCompositionId = 1L;
        int numSimulations = 3;

        NominalComposition nc = new NominalComposition();
        nc.setId(nominalCompositionId);
        nc.setName(nominalCompositionName);

        ScheduleExplorationRequest request = new ScheduleExplorationRequest();
        request.setNumSimulations(numSimulations);

        /*
         * Mockito configuration:
         *
         * When the 'compositionRepo.findByName(...)' method is called with the argument
         * 'nominalCompositionName', then return 'Optional.of(nc)' instead of actually
         * querying a database.
         *
         * Methods from 'runRepo' and 'mlopsProperties' are mocked in the same way.
         *
         * NOTE: the class 'java.util.Optional<T>' is a container object introduced in
         *  Java 8 to represent a value that may or may not be present. It helps reduce
         *  the need for explicit null checks and prevents NullPointerException in many cases.
         *  Not in this case, but with that class, it is possible to achieve the same goal
         *  of the safe navigation operator ?. that Groovy has.
         */
        when(compositionRepo.findByName(nominalCompositionName)).thenReturn(Optional.of(nc));
        when(runRepo.findMaxRunNumberByNominalCompositionId(nominalCompositionId)).thenReturn(Optional.of(0));
        when(mlopsProperties.getDataRoot()).thenReturn(dataRoot);

        /*
         * Mockito configuration:
         *
         * Mock 'SimulationDirectories' (manual wiring).
         *
         * Create a fake instance of 'SimulationDirectory' that it can be controlled in
         * this unit test; but that doesn’t do any real work.
         */
        SimulationDirectory subDir = mock(SimulationDirectory.class);
        SimulationDirectory runDir = mock(SimulationDirectory.class);
        when(runDir.getNumber()).thenReturn(1);
        when(runDir.getChildren()).thenReturn(List.of(subDir));

        SimulationDirectory nominalDir = mock(SimulationDirectory.class);
        when(nominalDir.getChildren()).thenReturn(List.of(runDir));

        SimulationDirectories dirs = mock(SimulationDirectories.class);
        when(dirs.getNominalCompositionDir()).thenReturn(nominalDir);

        /*
         * Mockito configuration:
         *
         * When the method 'load(true)' is called on the mock 'dirs', do nothing
         * (i.e., suppress any real behavior and return void silently).
         */
        doNothing().when(dirs).load(true);

        /*
         * Mockito configuration:
         *
         * Spy the service to inject mock 'SimulationDirectories'. It will create a partial
         * mock of the real 'DataOpsService' object and its real methods will run unless they
         * are explicitly stub or override.
         */
        DataOpsService service = spy(new DataOpsService(compositionRepo, runRepo, mlopsProperties));

        /*
         * Mockito configuration:
         *
         * A spy is a Mockito object that wraps a real instance but allows overriding just
         * specific methods and let the rest of the object behave normally.
         *
         * This is what we have when the protected method createSimulationDirectories is called.
         *
         * Why 'doReturn(...).when(...)' instead of 'when(...).thenReturn(...)'?
         * Because service is a spy, and Mockito’s 'when(...).thenReturn(...)' eagerly calls
         * the real method before it can stub it.
         */
        IntStream.rangeClosed(1, numSimulations).forEach( runNumber ->
                doReturn(dirs).when(service).createSimulationDirectories(nominalCompositionName, dataRoot, runNumber, numSimulations)
        );

        /*
         * Mockito configuration:
         *
         * Mock the factory static call (optionally replace with injectable strategy).
         *
         * Configures Mockito to intercept all static method calls to 'SimulationArtifactFactory'
         * within this test scope, so it is possible to control their return values.
         */
        List<SimulationArtifact> fakeArtifacts = List.of(mock(SimulationArtifact.class));

        /*
         * Mockito configuration:
         *
         * By default, Mockito cannot mock static methods — they are hardwired at the class level.
         *
         * Telling Mockito that the static method calls to 'SimulationArtifactFactory' will  be
         * intercepted and Mockito returns a 'MockedStatic' object that can be used to define behavior.
         */
        MockedStatic<SimulationArtifactFactory> factory = mockStatic(SimulationArtifactFactory.class);

        /*
         * Here the call to the static method 'load' is being intercepted and its behavior is being
         * mocked to return 'fakeArtifacts', instead of doing real file discovery/parsing.
         */
        factory.when(() -> SimulationArtifactFactory.load(eq(nominalCompositionName), any(), eq(subDir)))
                .thenReturn(fakeArtifacts);

        // Acting to get the response payload in the same way the controller does
        NominalCompositionDto result = service.scheduleExploration(nominalCompositionName, request);

        System.out.println("ARYLOG: result.toString() -> " + result.toString());

        // Asserting with Junit 5
        assertNotNull(result);
        assertEquals(numSimulations, result.getRuns().size());
        assertEquals(SimulationStatus.SCHEDULED, result.getRuns().get(0).getStatus());

        factory.close(); // Closing the static mock to avoid test pollution.

    }

}
