package org.doi.prmv4p113603.mlops.exception;

public class NominalCompositionDeletionException extends DeletionNotAllowedException {

    public NominalCompositionDeletionException(String name) {
        super("Cannot delete NominalComposition " + name + " because it has associated Runs.");
    }
}
