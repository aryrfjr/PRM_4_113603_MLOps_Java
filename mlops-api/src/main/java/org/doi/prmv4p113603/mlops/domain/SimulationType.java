package org.doi.prmv4p113603.mlops.domain;

/**
 * Just a controlled vocabulary for simulation type.
 */
public enum SimulationType {

    GENERATE_EXPLORATION,
    GENERATE_EXPLOITATION,
    ETL;

    public boolean isGenerateExploration() {
        return this == GENERATE_EXPLORATION;
    }

    public boolean isGenerateExploitation() {
        return this == GENERATE_EXPLOITATION;
    }

    public boolean isEtl() {
        return this == GENERATE_EXPLOITATION;
    }

}
