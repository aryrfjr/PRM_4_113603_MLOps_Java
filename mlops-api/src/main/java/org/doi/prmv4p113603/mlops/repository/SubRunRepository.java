package org.doi.prmv4p113603.mlops.repository;

import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * DAO for SubRunR entity. It provides built-in CRUD operations.
 */
public interface SubRunRepository extends JpaRepository<NominalComposition, Long> {

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
     * @param runId The id of a Run
     * @param subRunNumbers The SubRun numbers
     * @return true if any of the SubRun numbers exist
     */
    @Query("SELECT COUNT(sr) > 0 FROM SubRun sr WHERE sr.run.id = :runId AND sr.subRunNumber IN :subRunNumbers")
    boolean existsAnyByRunIdAndSubRunNumbers(@Param("runId") Long runId, @Param("subRunNumbers") List<Integer> subRunNumbers);

}
