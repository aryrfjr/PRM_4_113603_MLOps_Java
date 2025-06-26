package org.doi.prmv4p113603.mlops.exception;

public class DuplicatedNominalCompositionException extends InsertionNotAllowedException {

    public DuplicatedNominalCompositionException(String name) {
        super("Cannot create new NominalComposition with name " + name + " because it is duplicated.");
    }
}
