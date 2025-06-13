package org.doi.prmv4p113603.mlops.repository;

import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO for SubRunR entity. It provides built-in CRUD operations.
 */
public interface SubRunRepository extends JpaRepository<NominalComposition, Long> {
}
