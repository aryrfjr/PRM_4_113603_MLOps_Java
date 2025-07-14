package org.doi.prmv4p113603.mlops.repository;

import org.doi.prmv4p113603.mlops.model.Run;
import org.doi.prmv4p113603.mlops.model.SubRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * DAO for SubRunR entity. It provides built-in CRUD operations.
 */
public interface SubRunRepository extends JpaRepository<SubRun, Long> {

    /*
     * SELECT COUNT(*) > 0
     * FROM sub_runs
     * WHERE run_id = :runId
     *   AND sub_run_number IN (:subRunNumbers);
     *
     * ======================================================
     *
     * SELECT EXISTS (
     *     SELECT 1
     *     FROM sub_runs
     *     WHERE run_id = :runId
     *       AND sub_run_number IN (:subRunNumbers)
     * );
     */

    /**
     * Checks if a NominalComposition exists (by name) and that the specified
     * SubRuns do not already exist (based on runId and a set of subRunNumbers).
     *
     * @param runId         The id of a Run
     * @param subRunNumbers The SubRun numbers
     * @return true if any of the SubRun numbers exist
     */
    @Query("SELECT COUNT(sr) > 0 FROM SubRun sr WHERE sr.run.id = :runId AND sr.subRunNumber IN :subRunNumbers")
    boolean existsAnyByRunIdAndSubRunNumbers(@Param("runId") Long runId, @Param("subRunNumbers") List<Integer> subRunNumbers);

    /**
     * Retrieves all {@link SubRun} entities with the given Run ID.
     * <p>
     * This method's name allows JPA to derive the query by naming convention.
     *
     * @param runId the ID of a given {@link Run}.
     * @return a list of {@link SubRun} entities.
     */
    List<SubRun> findAllByRunId(Long runId);

    /**
     * Retrieves a {@link SubRun} entity with the given Run object and a sub_run_number.
     * <p>
     * This method's name allows JPA to derive the query by naming convention.
     *
     * @param run          and instance of a given {@link Run}.
     * @param subRunNumber a given sub-Run number.
     * @return a {@link SubRun} entity.
     */
    Optional<SubRun> findByRunAndSubRunNumber(Run run, int subRunNumber);

    /**
     * Retrieves all {@link SubRun} entities with the given Run object and a sub_run_number in the list.
     * <p>
     * This method's name allows JPA to derive the query by naming convention.
     *
     * @param run           and instance of a given {@link Run}.
     * @param subRunNumbers a set of sub-Run numbers.
     * @return a set of {@link SubRun} entities.
     */
    List<SubRun> findAllByRunAndSubRunNumberIn(Run run, List<Integer> subRunNumbers);

}
