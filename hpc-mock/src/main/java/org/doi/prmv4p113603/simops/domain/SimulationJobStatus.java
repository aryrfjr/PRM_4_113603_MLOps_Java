package org.doi.prmv4p113603.simops.domain;

/**
 * Just a controlled vocabulary for simulation job status that will be persisted in the DB.
 */
public enum SimulationJobStatus {

    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED;

    public boolean isQueued() {
        return this == QUEUED;
    }

    public boolean isRunning() {
        return this == RUNNING;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

}
