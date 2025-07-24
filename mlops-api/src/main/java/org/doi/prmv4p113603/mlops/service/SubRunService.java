package org.doi.prmv4p113603.mlops.service;

import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.data.dto.RunDto;
import org.doi.prmv4p113603.mlops.data.dto.SubRunDto;
import org.doi.prmv4p113603.mlops.model.Run;
import org.doi.prmv4p113603.mlops.model.SubRun;
import org.doi.prmv4p113603.mlops.repository.SubRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer to encapsulate all business logic related to SubRun.
 */
@Service
@AllArgsConstructor
public class SubRunService {

    private final SubRunRepository repository;

    /**
     * Lists all SubRuns ordered by ID.
     */
    public List<SubRunDto> listAllByRunId(Long runId) {
        return repository.findAllByRunId(runId).stream()
                .map(SubRunDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RunDto> getAllSubRunsGroupedByRunDto() {

        List<SubRun> allSubRuns = repository.findAllWithRun();

        // Group SubRuns by Run
        Map<Run, List<SubRun>> grouped = allSubRuns.stream()
                .collect(Collectors.groupingBy(SubRun::getRun));

        // Map to RunDto including subRuns
        return grouped.entrySet().stream()
                .map(entry -> {
                    Run run = entry.getKey();
                    List<SubRun> subRuns = entry.getValue();

                    List<SubRunDto> subRunDtos = subRuns.stream()
                            .map(SubRunDto::fromEntity)
                            .collect(Collectors.toList());

                    RunDto dto = RunDto.fromEntity(run);
                    dto.setSubRuns(subRunDtos);
                    return dto;
                })
                .collect(Collectors.toList());
    }

}
