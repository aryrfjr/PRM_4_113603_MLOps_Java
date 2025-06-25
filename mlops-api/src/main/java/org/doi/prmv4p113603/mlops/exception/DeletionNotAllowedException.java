package org.doi.prmv4p113603.mlops.exception;

public abstract class DeletionNotAllowedException extends RuntimeException {

    // NOTE: It's abstract because this is not going to be thrown directly; always a subclass.

    public DeletionNotAllowedException(String message) {
        super(message);
    }

    public DeletionNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
