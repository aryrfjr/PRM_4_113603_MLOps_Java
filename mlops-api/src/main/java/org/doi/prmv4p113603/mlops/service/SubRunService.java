package org.doi.prmv4p113603.mlops.service;

import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.data.dto.SubRunDto;
import org.doi.prmv4p113603.mlops.repository.SubRunRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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

}
