package org.doi.prmv4p113603.mlops.service;

import org.doi.prmv4p113603.mlops.data.dto.RunDto;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExplorationRequest;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.doi.prmv4p113603.mlops.repository.NominalCompositionRepository;
import org.doi.prmv4p113603.mlops.repository.RunRepository;
import org.doi.prmv4p113603.mlops.util.FileSystemUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class DataOpsServiceTest {

    @Mock
    private NominalCompositionRepository compositionRepo;

    @Mock
    private RunRepository runRepo;

    @InjectMocks
    private DataOpsService dataOpsService;

    @Test
    public void testScheduleExploration_Success() {
        // Arrange
        String compositionName = "MgO";
        ScheduleExplorationRequest request = new ScheduleExplorationRequest();
        request.setNumSimulations(2); // simulate 2 subruns

        NominalComposition nc = new NominalComposition();
        nc.setId(1L);
        nc.setName("MgO");

        when(compositionRepo.findByName("MgO")).thenReturn(Optional.of(nc));
        when(runRepo.findMaxRunNumberByNominalCompositionId(1L)).thenReturn(Optional.of(1)); // nextRun = 2

        // Mock FileSystemUtils for existing sub_run directories and artifact detection
        mockStatic(FileSystemUtils.class).useMock(mocked -> {
            for (int i = 1; i <= 2; i++) {
                String subRunPath = "/data/MgO/run_2/sub_run_" + i;
                when(FileSystemUtils.join(any(), any(), any(), any())).thenReturn(subRunPath);
                when(FileSystemUtils.pathExists(subRunPath)).thenReturn(true);

                DirectoryStream<Path> mockStream = mock(DirectoryStream.class);
                when(FileSystemUtils.listFiles(subRunPath)).thenReturn(mockStream);
                when(mockStream.iterator()).thenReturn(Collections.emptyIterator()); // no artifacts
            }

            // Act
            RunDto runDto = dataOpsService.scheduleExploration("MgO", request);

            // Assert
            assertEquals(2, runDto.getSubRuns().size());
            assertEquals("SCHEDULED", runDto.getStatus());
        });
    }
}
