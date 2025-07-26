package org.doi.prmv4p113603.mlops.service;

import lombok.RequiredArgsConstructor;
import org.doi.prmv4p113603.mlops.data.dto.RunDto;
import org.doi.prmv4p113603.mlops.domain.RunStatus;
import org.doi.prmv4p113603.mlops.domain.SimulationArtifactRole;
import org.doi.prmv4p113603.mlops.domain.SimulationDirectory;
import org.doi.prmv4p113603.mlops.exception.NominalCompositionNotFoundException;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.doi.prmv4p113603.mlops.model.Run;
import org.doi.prmv4p113603.mlops.model.SubRun;
import org.doi.prmv4p113603.mlops.repository.NominalCompositionRepository;
import org.doi.prmv4p113603.mlops.repository.RunRepository;
import org.doi.prmv4p113603.mlops.util.SimulationArtifactFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer to encapsulate all business logic related to Run.
 */
@Service
@RequiredArgsConstructor
public class RunService {

    private final RunRepository repository;
    private final NominalCompositionRepository compositionRepo;

    public NominalComposition loadCompositionOrThrow(String name) {
        return compositionRepo.findByName(name)
                .orElseThrow(() -> new NominalCompositionNotFoundException(name));
    }

    public int findNextRunNumber(NominalComposition composition) {
        return repository.findMaxRunNumberByNominalCompositionId(composition.getId()).orElse(0) + 1;
    }

    public List<Run> createRunsFromDirectories(NominalComposition composition, List<SimulationDirectory> runDirs) {
        List<Run> runs = new ArrayList<>();

        for (SimulationDirectory runDir : runDirs) {
            Run run = Run.builder()
                    .nominalComposition(composition)
                    .runNumber(runDir.getNumber())
                    .status(RunStatus.EXPLORATION_SCHEDULED)
                    .build();

            SubRun subRun = SubRun.builder()
                    .run(run)
                    .subRunNumber(0)
                    .build();

            subRun.setSimulationArtifacts(SimulationArtifactFactory.load(
                    composition.getName(),
                    subRun,
                    runDir.getChildren().get(0),
                    SimulationArtifactRole.GENERATE_IO
            ));

            run.setSubRuns(List.of(subRun));
            runs.add(run);
        }

        return runs;
    }

    public void saveAll(List<Run> runs) {
        repository.saveAll(runs);
    }

    public List<Run> findAllByComposition(NominalComposition composition) {
        return repository.findAllByNominalCompositionId(composition.getId());
    }
    
    /**
     * Lists all Runs ordered by ID.
     */
    public List<RunDto> listAllByNominalCompositionId(Long nominalCompositionId) {
        return repository.findAllByNominalCompositionId(nominalCompositionId).stream()
                .map(RunDto::fromEntity)
                .collect(Collectors.toList());
    }

}
