package org.doi.prmv4p113603.mlops.exception;

public class SimulationArtifactNotFoundException extends EntityNotFoundException {

    public SimulationArtifactNotFoundException(String name) {
        super("Simulation artifact " + name + " not found.");
    }

}
