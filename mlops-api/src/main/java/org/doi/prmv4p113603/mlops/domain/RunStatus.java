package org.doi.prmv4p113603.mlops.domain;

/**
 * Represents the execution state of a SubRun or Run.
 */
public enum RunStatus {

    EXPLORATION_SCHEDULED,
    EXPLORATION_RUNNING,
    ETL_COMPLETED,
    EXPLORATION_FAILED,
    ETL_FAILED;

    public boolean isExplorationScheduled() {
        return this == EXPLORATION_SCHEDULED;
    }

    public boolean isExplorationRunning() {
        return this == EXPLORATION_RUNNING;
    }

    public boolean isEtlCompleted() {
        return this == ETL_COMPLETED;
    }

    public boolean isExplorationFailed() {
        return this == EXPLORATION_FAILED;
    }

    public boolean isEtlFailed() {
        return this == ETL_FAILED;
    }

}
