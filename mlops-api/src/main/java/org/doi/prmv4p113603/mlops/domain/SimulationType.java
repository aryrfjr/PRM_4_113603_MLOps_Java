package org.doi.prmv4p113603.mlops.domain;

/**
 * Just a controlled vocabulary for simulation type.
 */
public enum SimulationType {

    EXPLORATION,
    EXPLOITATION;

    public boolean isExploration() {
        return this == EXPLORATION;
    }
    public boolean isExploitation() {
        return this == EXPLOITATION;
    }

}
