package org.doi.prmv4p113603.mlops.domain;

/**
 * Just a controlled vocabulary for simulation artifact role that will be persisted in the DB.
 */
public enum SimulationArtifactRole {

    GENERATE_IO,
    GENERATE_INPUT,
    GENERATE_OUTPUT,
    ETL_IO,
    ETL_INPUT,
    ETL_OUTPUT;

    public boolean isGenerateIo() {
        return this == GENERATE_IO;
    }

    public boolean isGenerateInput() {
        return this == GENERATE_INPUT;
    }

    public boolean isGenerateOutput() {
        return this == GENERATE_OUTPUT;
    }

    public boolean isEtlIo() {
        return this == GENERATE_IO;
    }

    public boolean isEtlInput() {
        return this == ETL_INPUT;
    }

    public boolean isEtlOutput() {
        return this == ETL_OUTPUT;
    }

}
