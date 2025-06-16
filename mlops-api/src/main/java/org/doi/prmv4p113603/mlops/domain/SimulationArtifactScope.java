package org.doi.prmv4p113603.mlops.domain;

/**
 * Just a controlled vocabulary for simulation artifact scoping. Specifies the scope of a
 * directory containing {@code SimulationArtifact}s, indicating whether it belongs
 * to the Nominal Composition root directory, to an exploration simulation {@code Run}
 * or a specific exploitation simulation {@code SubRun}.
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * if (artifact.getSimulationArtifactScope() == SimulationArtifactScope.SUBRUN) {
 *     // Handle sub-run-specific logic
 * }
 * }</pre>
 *
 * @see SimulationArtifact
 * @see SimulationArtifactType
 */
public enum SimulationArtifactScope {

    NOMINAL_COMPOSITION,
    RUN,
    SUB_RUN;

    public boolean isNominalComposition() {
        return this == RUN;
    }

    public boolean isRun() {
        return this == RUN;
    }

    public boolean isSubRun() {
        return this == SUB_RUN;
    }

}
