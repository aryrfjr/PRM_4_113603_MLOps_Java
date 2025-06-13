package org.doi.prmv4p113603.mlops.repository;

import org.doi.prmv4p113603.mlops.model.Run;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * DAO for Run entity. It provides built-in CRUD operations and custom find methods.
 */
public interface RunRepository extends JpaRepository<Run, Long> {

    /*
     * NOTE: The method findMaxRunNumberByNominalCompositionId is explicitly
     * implemented using a JPQL (Java Persistence Query Language) query via
     * the @Query annotation. See more about the limits of Spring Data’s query derivation syntax
     * at: https://docs.spring.io/spring-data/jpa/reference/#jpa.query-methods.query-creation
     */

    /**
     * Finds the maximum run number associated with a given nominal composition ID.
     *
     * @param nominalCompositionId the foreign key reference
     * @return the highest run number or empty if none
     */
    @Query("SELECT MAX(r.runNumber) FROM Run r WHERE r.nominalComposition.id = :nominalCompositionId")
    Optional<Integer> findMaxRunNumberByNominalCompositionId(@Param("nominalCompositionId") Long nominalCompositionId);

}
