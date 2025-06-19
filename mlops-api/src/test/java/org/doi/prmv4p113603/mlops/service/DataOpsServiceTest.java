package org.doi.prmv4p113603.mlops.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.doi.prmv4p113603.mlops.config.MinioProperties;
import org.doi.prmv4p113603.mlops.config.MlopsProperties;
import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionDto;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExploitationRequest;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExplorationRequest;
import org.doi.prmv4p113603.mlops.domain.*;
import org.doi.prmv4p113603.mlops.model.*;
import org.doi.prmv4p113603.mlops.repository.NominalCompositionRepository;
import org.doi.prmv4p113603.mlops.repository.RunRepository;
import org.doi.prmv4p113603.mlops.repository.SubRunRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.*;

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
    SubRunRepository subRunRepo;

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
         * Spy the service to inject mock 'SimulationDirectories'. It will create a partial
         * mock of the real 'DataOpsService' object and its real methods will run unless they
         * are explicitly stub or override.
         */

        SimulationDirectoriesFactory simulationDirectoriesFactory = new SimulationDirectoriesFactory(
                mlopsProperties,
                Mockito.mock(MinioStorageService.class));

        DataOpsService service = spy(new DataOpsService(
                compositionRepo,
                simulationDirectoriesFactory,
                runRepo,
                subRunRepo));

        /*
         * Acting to get the response payload in the same way the controller does.
         *
         * NOTE: that process will scan directories, what will turn this unit test into
         *  an integration test, since we will be testing how the service integrates
         *  with the real filesystem. There are some trade-offs since there is an
         *  external dependence introduced but that data discovery portion is needed here.
         */
        NominalCompositionDto result = service.scheduleExploration(nominalCompositionName, request);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String resultJson = mapper.writeValueAsString(result);

        System.out.println("DataOpsService.scheduleExploration -> resultJson -> " + resultJson);

        // Asserting with Junit 5
        assertNotNull(result);
        assertEquals(numSimulations, result.getRuns().size());
        assertEquals(SimulationStatus.SCHEDULED, result.getRuns().get(0).getStatus());

    }

    @Test
    void testScheduleExploitation_success() throws Exception {

        // TODO: set in a file 'application.properties' for tests
        final String dataRoot = "/home/aryjr/fromiomega/pos-doc/UFSCar/MG-NMR/ML/big-data-full/";

        // Starting Mockito setup
        String nominalCompositionName = "Zr49Cu49Al2";
        long nominalCompositionId = 1L;

        NominalComposition nc = new NominalComposition();
        nc.setId(nominalCompositionId);
        nc.setName(nominalCompositionName);

        Run run1 = Run.builder().runNumber(1).status(SimulationStatus.SCHEDULED).build();
        Run run2 = Run.builder().runNumber(2).status(SimulationStatus.SCHEDULED).build();
        List<Run> runs = List.of(run1, run2);

        ScheduleExploitationRequest.RunInput runInput1 = new ScheduleExploitationRequest.RunInput();
        runInput1.setId(1L);
        runInput1.setRunNumber(1);
        runInput1.setSubRuns(List.of(1, 2, 3, 4, 5));

        ScheduleExploitationRequest.RunInput runInput2 = new ScheduleExploitationRequest.RunInput();
        runInput2.setId(2L);
        runInput2.setRunNumber(2);
        runInput2.setSubRuns(List.of(6, 13, 14));

        ScheduleExploitationRequest request = new ScheduleExploitationRequest();
        request.setRuns(List.of(runInput1, runInput2));

        when(compositionRepo.findByName(nominalCompositionName)).thenReturn(Optional.of(nc));
        when(runRepo.findAllByIdIn(List.of(1L, 2L))).thenReturn(runs);
        when(mlopsProperties.getDataRoot()).thenReturn(dataRoot);

        SimulationDirectoriesFactory simulationDirectoriesFactory = new SimulationDirectoriesFactory(
                mlopsProperties,
                Mockito.mock(MinioStorageService.class));

        DataOpsService service = spy(new DataOpsService(
                compositionRepo,
                simulationDirectoriesFactory,
                runRepo,
                subRunRepo));

        NominalCompositionDto result = service.scheduleExploitation(nominalCompositionName, request);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String resultJson = mapper.writeValueAsString(result.getRuns().get(0));

        System.out.println("DataOpsService.scheduleExploitation -> resultJson -> " + resultJson);

        // Asserting with Junit 5
        assertNotNull(result);
        assertEquals(SimulationStatus.SCHEDULED, result.getRuns().get(0).getStatus());

    }

}
