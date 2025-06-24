package org.doi.prmv4p113603.mlops.repository;

import org.doi.prmv4p113603.mlops.model.SimulationArtifact;
import org.doi.prmv4p113603.mlops.model.SubRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * DAO for SimulationArtifact entity. It provides built-in CRUD operations.
 */
public interface SimulationArtifactRepository extends JpaRepository<SimulationArtifact, Long> {

    /**
     * Retrieves all {@link SimulationArtifact} entities with the given SubRun ID.
     * <p>
     * This method's name allows JPA to derive the query by naming convention.
     *
     * @param subRunId the ID of a given {@link SubRun}.
     * @return a list of {@link SimulationArtifact} entities.
     */
    List<SimulationArtifact> findAllBySubRunId(Long subRunId);

}
