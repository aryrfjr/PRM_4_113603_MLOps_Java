package org.doi.prmv4p113603.mlops.service;

import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.data.dto.SimulationArtifactDto;
import org.doi.prmv4p113603.mlops.repository.SimulationArtifactRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer to encapsulate all business logic related to SimulationArtifact entity.
 */
@Service
@AllArgsConstructor
public class SimulationArtifactService {

    private final SimulationArtifactRepository repository;

    /**
     * Lists all SimulationArtifact entities filtered by SubRun ID and ordered by ID.
     */
    public List<SimulationArtifactDto> listAllBySubRunId(Long subRunId) {
        return repository.findAllBySubRunId(subRunId).stream()
                .map(SimulationArtifactDto::fromEntity)
                .collect(Collectors.toList());
    }

}
