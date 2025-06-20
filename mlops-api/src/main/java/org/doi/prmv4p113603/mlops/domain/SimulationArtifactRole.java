package org.doi.prmv4p113603.mlops.domain;

/**
 * Just a controlled vocabulary for simulation artifact role that will be persisted in the DB.
 */
public enum SimulationArtifactRole {

    INPUT,
    OUTPUT;

    public boolean isInput() {
        return this == INPUT;
    }

    public boolean isOutput() {
        return this == OUTPUT;
    }


}
