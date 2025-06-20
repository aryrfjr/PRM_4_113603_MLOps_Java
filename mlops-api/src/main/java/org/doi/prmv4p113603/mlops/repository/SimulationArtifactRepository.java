package org.doi.prmv4p113603.mlops.repository;

import org.doi.prmv4p113603.mlops.model.SimulationArtifact;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO for SimulationArtifact entity. It provides built-in CRUD operations.
 */
public interface SimulationArtifactRepository extends JpaRepository<SimulationArtifact, Long> {
}
