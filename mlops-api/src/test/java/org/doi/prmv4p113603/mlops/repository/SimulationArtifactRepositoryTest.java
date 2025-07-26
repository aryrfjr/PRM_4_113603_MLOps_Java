package org.doi.prmv4p113603.mlops.repository;

import org.doi.prmv4p113603.mlops.model.*;
import org.doi.prmv4p113603.mlops.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * NOTE: to run this test: ./mvnw test -Dspring.profiles.active=test
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class SimulationArtifactRepositoryTest {

    @Autowired
    private SimulationArtifactRepository simulationArtifactRepository;

    @Autowired
    private SubRunRepository subRunRepository;

    @Autowired
    private RunRepository runRepository;

    @Autowired
    private NominalCompositionRepository nominalCompositionRepository;

    @Test
    void shouldPersistAndRetrieveSimulationArtifact() {
        // Given
        NominalComposition nc = NominalComposition.builder()
                .name("Zr47Cu47Al6")
                .description("Zr47Cu47Al6 test")
                .createdBy("test")
                .createdAt(Instant.now())
                .build();

        nc = nominalCompositionRepository.save(nc);

        Run run = Run.builder()
                .nominalComposition(nc)
                .runNumber(1)
                .status(RunStatus.EXPLORATION_SCHEDULED)
                .createdBy("test")
                .createdAt(Instant.now())
                .build();

        run = runRepository.save(run);

        SubRun subRun = SubRun.builder()
                .run(run)
                .subRunNumber(1)
                .createdBy("test")
                .createdAt(Instant.now())
                .build();

        subRun = subRunRepository.save(subRun);

        SimulationArtifact artifact = SimulationArtifact.builder()
                .subRun(subRun)
                .artifactType(SimulationArtifactType.ICOHPLIST)
                .artifactRole(SimulationArtifactRole.GENERATE_OUTPUT)
                .filePath("/fake/path")
                .build();

        simulationArtifactRepository.save(artifact);

        // When
        List<SimulationArtifact> artifacts = simulationArtifactRepository.findAll();

        // Then
        assertThat(artifacts).hasSize(1);
        assertThat(artifacts.get(0).getArtifactType()).isEqualTo(SimulationArtifactType.ICOHPLIST);
    }

}
