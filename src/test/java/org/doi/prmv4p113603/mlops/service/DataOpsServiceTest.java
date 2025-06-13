package org.doi.prmv4p113603.mlops.service;

import org.doi.prmv4p113603.mlops.data.dto.RunDto;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExplorationRequest;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.doi.prmv4p113603.mlops.model.Run;
import org.doi.prmv4p113603.mlops.repository.NominalCompositionRepository;
import org.doi.prmv4p113603.mlops.repository.RunRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DataOpsServiceTest {

    @Mock
    private NominalCompositionRepository compositionRepo;

    @Mock
    private RunRepository runRepo;

    @InjectMocks
    private DataOpsService dataOpsService;

    @Mock
    private FileSystemService fileSystem;

    @Test
    void testScheduleExploration_nominalCase() {
        // Given
        String compositionName = "MgO";
        ScheduleExplorationRequest request = new ScheduleExplorationRequest();
        request.setNumSimulations(1);

        // Mock NominalComposition
        NominalComposition mockComp = new NominalComposition();
        mockComp.setId(42L);
        mockComp.setName(compositionName);

        when(compositionRepo.findByName(eq(compositionName)))
                .thenReturn(Optional.of(mockComp));

        // Mock next run number
        when(runRepo.findMaxRunNumberByNominalCompositionId(eq(42L)))
                .thenReturn(Optional.of(4)); // nextRunNumber should be 5

        // Mock the save (optional: you can just verify it was called)
        when(runRepo.save(any(Run.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // return same Run

        // Stub filesystem utils as needed here...

        // When
        RunDto result = dataOpsService.scheduleExploration(compositionName, request);

        // Then
        assertEquals("SCHEDULED", result.getStatus());
        assertEquals(1, result.getSubRuns().size());

        verify(runRepo).save(any(Run.class));
        verify(compositionRepo).findByName(compositionName);
    }
}
