package org.doi.prmv4p113603.mlops.repository;

import org.doi.prmv4p113603.mlops.model.ExplorationPipelineRun;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO for ExplorationPipelineRun entity. It provides built-in CRUD operations and custom find methods.
 */
public interface ExplorationPipelineRunRepository extends JpaRepository<ExplorationPipelineRun, Long> {}
