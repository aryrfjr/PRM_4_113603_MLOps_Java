package org.doi.prmv4p113603.mlops.service;

import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionDto;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExplorationRequest;
import org.doi.prmv4p113603.mlops.domain.SimulationStatus;
import org.doi.prmv4p113603.mlops.model.*;
import org.doi.prmv4p113603.mlops.repository.NominalCompositionRepository;
import org.doi.prmv4p113603.mlops.repository.RunRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @Mock
    private NominalCompositionRepository compositionRepo;

    @Mock
    private RunRepository runRepo;

    @InjectMocks
    private DataOpsService dataOpsService;

    @Test
    void testScheduleExploration_success() {

        // Arrange
        String compositionName = "Zr49Cu49Al2";
        int numSimulations = 2;
        long compositionId = 1L;

        ScheduleExplorationRequest request = new ScheduleExplorationRequest();
        request.setNumSimulations(numSimulations);

        NominalComposition composition = new NominalComposition();
        composition.setId(compositionId);
        composition.setName(compositionName);

        when(compositionRepo.findByName(compositionName))
                .thenReturn(Optional.of(composition));

        when(runRepo.findMaxRunNumberByNominalCompositionId(compositionId))
                .thenReturn(Optional.of(5));  // Next run will be 6

        // Set up path checks for both runs
//        for (int runNumber = 6; runNumber < 6 + numSimulations; runNumber++) {
//
//            String runDir = DataOpsService.DATA_ROOT + "/" + compositionName + "/c/md/lammps/100/" + runNumber;
//            String subRunDir = runDir + "/2000/0";
//
//        }

        // Mock save (even though it's not asserted here)
        when(runRepo.save(any(Run.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        NominalCompositionDto result = dataOpsService.scheduleExploration(compositionName, request);

        // Assert
        assertNotNull(result);
        assertEquals(compositionId, result.getId());
        assertEquals(numSimulations, result.getRuns().size());
        result.getRuns().forEach(runDto -> {
            assertEquals(SimulationStatus.SCHEDULED, runDto.getStatus());
            assertEquals(1, runDto.getSubRuns().size());
            assertEquals(0, runDto.getSubRuns().get(0).getSubRunNumber());
        });

        verify(compositionRepo).findByName(compositionName);
        verify(runRepo).findMaxRunNumberByNominalCompositionId(compositionId);
    }


}
