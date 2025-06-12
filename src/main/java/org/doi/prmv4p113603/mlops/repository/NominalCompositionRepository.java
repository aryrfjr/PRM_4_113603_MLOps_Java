package org.doi.prmv4p113603.mlops.repository;

import org.doi.prmv4p113603.mlops.model.NominalComposition;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * DAO for NominalComposition entity.
 * <p>
 * Provides built-in CRUD operations and custom finder by name.
 */
public interface NominalCompositionRepository extends JpaRepository<NominalComposition, Long> {
    Optional<NominalComposition> findByName(String name);
    List<NominalComposition> findAllByOrderByNameAsc();
}
