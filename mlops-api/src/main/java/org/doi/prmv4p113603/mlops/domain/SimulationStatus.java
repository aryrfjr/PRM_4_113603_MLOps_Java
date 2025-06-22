package org.doi.prmv4p113603.mlops.domain;

/**
 * Represents the execution state of a SubRun or Run.
 */
public enum SimulationStatus {
    SCHEDULED,
    RUNNING,
    COMPLETED,
    FAILED;

    public boolean isScheduled() {
        return this == SCHEDULED;
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
