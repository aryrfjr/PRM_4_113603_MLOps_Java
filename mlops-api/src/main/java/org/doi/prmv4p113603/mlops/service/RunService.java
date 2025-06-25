package org.doi.prmv4p113603.mlops.service;

import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.data.dto.RunDto;
import org.doi.prmv4p113603.mlops.repository.RunRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer to encapsulate all business logic related to Run.
 */
@Service
@AllArgsConstructor
public class RunService {

    private final RunRepository repository;

    /**
     * Lists all Runs ordered by ID.
     */
    public List<RunDto> listAllByNominalCompositionId(Long nominalCompositionId) {
        return repository.findAllByNominalCompositionId(nominalCompositionId).stream()
                .map(RunDto::fromEntity)
                .collect(Collectors.toList());
    }

}
