package org.doi.prmv4p113603.mlops.domain;

/**
 * Represents the execution state of a SubRun or Run.
 */
public enum SimulationStatus {
    SCHEDULED,
    RUNNING,
    COMPLETED,
    FAILED
}
