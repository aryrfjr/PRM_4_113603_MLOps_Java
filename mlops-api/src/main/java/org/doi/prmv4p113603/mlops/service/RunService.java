package org.doi.prmv4p113603.mlops.service;

import org.doi.prmv4p113603.mlops.data.dto.RunDto;
import org.doi.prmv4p113603.mlops.repository.RunRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer to encapsulate all business logic related to Run.
 */
@Service
public class RunService {

    private final RunRepository repository;

    public RunService(RunRepository repository) {
        this.repository = repository;
    }

    /**
     * Lists all Runs ordered by name.
     */
    public List<RunDto> listAllByNominalCompositionId(Long nominalCompositionId) {
        return repository.findAllByNominalCompositionId(nominalCompositionId).stream()
                .map(RunDto::fromEntity)
                .collect(Collectors.toList());
    }

}
