package org.doi.prmv4p113603.mlops.repository;

import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.doi.prmv4p113603.mlops.model.Run;
import org.doi.prmv4p113603.mlops.model.SubRun;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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
     * Finds the maximum run number (how many  {@link Run} entities) associated with a given nominal composition ID.
     *
     * @param nominalCompositionId the foreign key reference
     * @return the highest run number or empty if none
     */
    @Query("SELECT MAX(r.runNumber) FROM Run r WHERE r.nominalComposition.id = :nominalCompositionId")
    Optional<Integer> findMaxRunNumberByNominalCompositionId(@Param("nominalCompositionId") Long nominalCompositionId);

    /**
     * Retrieves all {@link Run} entities with associated {@link SubRun} entities filtered by {@link Run} id.
     *
     * @param ids the set of Run ids
     * @return a collection of Runs
     */
    @Query("SELECT DISTINCT r FROM Run r LEFT JOIN FETCH r.subRuns WHERE r.id IN :ids")
    List<Run> findAllWithSubRunsById(@Param("ids") List<Long> ids);

    /**
     * Retrieves all {@link Run} entities with the given IDs, eagerly loading their associated {@link SubRun} entities.
     * <p>
     * This method uses a JPA EntityGraph to avoid the N+1 query problem when accessing the <code>subRuns</code> collection.
     *
     * @param ids the list of {@link Run} IDs to retrieve
     * @return a list of {@link Run} entities with their subRuns preloaded
     */
    @EntityGraph(attributePaths = "subRuns")
    List<Run> findAllByIdIn(List<Long> ids);

    /**
     * Retrieves all {@link Run} entities with the given NominalComposition ID.
     * <p>
     * This method's name allows JPA to derive the query by naming convention.
     *
     * @param nominalCompositionId the ID of a given {@link NominalComposition}.
     * @return a list of {@link Run} entities.
     */
    List<Run> findAllByNominalCompositionId(Long nominalCompositionId);

    /**
     * Checks if there are any {@link Run} entity associated to a given NominalComposition ID.
     * <p>
     * This method's name allows JPA to derive the query by naming convention.
     *
     * @param nominalCompositionId the ID of a given {@link NominalComposition}.
     * @return a boolean indicating if there are or not entities.
     */
    boolean existsByNominalCompositionId(Long nominalCompositionId);

}
