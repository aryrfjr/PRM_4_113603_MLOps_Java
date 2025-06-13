package org.doi.prmv4p113603.mlops.service;

import org.doi.prmv4p113603.mlops.data.dto.RunDto;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExplorationRequest;
import org.doi.prmv4p113603.mlops.model.*;
import org.doi.prmv4p113603.mlops.repository.NominalCompositionRepository;
import org.doi.prmv4p113603.mlops.repository.RunRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataOpsServiceTest {

    @Mock
    NominalCompositionRepository compositionRepo;

    @Mock
    RunRepository runRepo;

    @Mock
    FileSystemService fileSystem;

    @InjectMocks
    DataOpsService dataOpsService;

    @Test
    void testScheduleExploration_successfullySchedulesRun() throws IOException {
        // Given
        String compositionName = "MgO";
        int numSimulations = 2;

        ScheduleExplorationRequest request = new ScheduleExplorationRequest();
        request.setNumSimulations(numSimulations);

        NominalComposition composition = new NominalComposition();
        composition.setId(1L);
        composition.setName(compositionName);

        when(compositionRepo.findByName(compositionName)).thenReturn(Optional.of(composition));
        when(runRepo.findMaxRunNumberByNominalCompositionId(1L)).thenReturn(Optional.of(3)); // next run = 4

        // Mock filesystem for each subRun directory
        for (int i = 1; i <= numSimulations; i++) {
            String subRunPath = "/data/MgO/run_4/sub_run_" + i;

            when(fileSystem.join("/data", "MgO", "run_4", "sub_run_" + i))
                    .thenReturn(subRunPath);
            when(fileSystem.pathExists(subRunPath)).thenReturn(true);
            when(fileSystem.listFiles(subRunPath)).thenReturn(mockEmptyDirectoryStream());
        }

        when(fileSystem.isRegularFile(anyString())).thenReturn(true);
        when(fileSystem.getFileSize(anyString())).thenReturn(1024);
        when(fileSystem.calculateSHA256(anyString())).thenReturn("mocked-checksum");

        // Save method can just echo the input
        when(runRepo.save(any(Run.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RunDto result = dataOpsService.scheduleExploration(compositionName, request);

        // Then
        assertNotNull(result);
        assertEquals("SCHEDULED", result.getStatus());
        assertEquals(2, result.getSubRuns().size());

        verify(runRepo).save(any(Run.class));
    }

    private DirectoryStream<Path> mockEmptyDirectoryStream() {

        @SuppressWarnings("unchecked")
        DirectoryStream<Path> stream = (DirectoryStream<Path>) mock(DirectoryStream.class);

        when(stream.iterator()).thenReturn(Collections.emptyIterator());

        return stream;

    }

}
