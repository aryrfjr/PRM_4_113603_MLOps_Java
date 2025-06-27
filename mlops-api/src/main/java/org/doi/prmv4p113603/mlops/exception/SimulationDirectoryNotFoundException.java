package org.doi.prmv4p113603.mlops.exception;

public class SimulationDirectoryNotFoundException extends EntityNotFoundException {

    public SimulationDirectoryNotFoundException(String path) {
        super("Simulation directory " + path + " not found.");
    }

}
