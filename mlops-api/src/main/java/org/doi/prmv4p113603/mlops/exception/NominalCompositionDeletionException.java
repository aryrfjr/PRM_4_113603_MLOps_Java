package org.doi.prmv4p113603.mlops.exception;

public class NominalCompositionDeletionException extends DeletionNotAllowedException {

    public NominalCompositionDeletionException(Long id) {
        super("Cannot delete NominalComposition with ID " + id + " because it has associated Runs.");
    }
}
