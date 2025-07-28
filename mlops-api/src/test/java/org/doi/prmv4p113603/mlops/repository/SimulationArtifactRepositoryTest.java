package org.doi.prmv4p113603.mlops.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.doi.prmv4p113603.mlops.model.*;
import org.doi.prmv4p113603.mlops.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.doi.prmv4p113603.mlops.testutil.TestFixtures.*;

/*
 * NOTE: to run test lifecycle phase for this class: ./mvnw test -Dtest=SimulationArtifactRepositoryTest
 *
 * NOTE: Ensures only JPA components are loaded and the correct Spring profile (test_repo_pgsql) is used.
 *
 * NOTE: Disables default behavior of replacing DataSource with an in-memory database during testing,
 *  ensuring it uses the Dockerized PostgreSQL instead.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test_repo_pgsql") // Spring will load 'resources/application-test_repo_pgsql.yml'
class SimulationArtifactRepositoryTest {

    @Autowired
    private SimulationArtifactRepository simulationArtifactRepository;

    @Autowired
    private SubRunRepository subRunRepository;

    @Autowired
    private RunRepository runRepository;

    @Autowired
    private NominalCompositionRepository nominalCompositionRepository;

    @Autowired
    DataSource dataSource;

    @Test
    void shouldPersistAndRetrieveSimulationArtifact() {

        try (Connection conn = dataSource.getConnection()) {
            System.out.println("\nSpring is connected to: " + conn.getMetaData().getURL() + "\n");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Given
        NominalComposition nominalComposition = dummyNominalComposition(Optional.empty(), "Zr47Cu47Al6");

        nominalComposition = nominalCompositionRepository.save(nominalComposition);

        Run run = dummyRun(Optional.empty(), nominalComposition, 1, RunStatus.EXPLORATION_SCHEDULED, Optional.empty());

        run = runRepository.save(run);

        SubRun subRun = dummySubRun(Optional.empty(), run, 1, Optional.empty());

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

        System.out.println("\nSimulation Artifacts (toString): " + artifacts + "\n");

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        try {
            System.out.println("\nSimulation Artifacts (JSON): " + mapper.writeValueAsString(artifacts) + "\n");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

}
