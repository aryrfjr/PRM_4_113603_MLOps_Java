package org.doi.prmv4p113603.mlops.exception;

public class NominalCompositionNotFoundException extends EntityNotFoundException {

    public NominalCompositionNotFoundException(String name) {
        super("NominalComposition with name " + name + " not found.");
    }

}
