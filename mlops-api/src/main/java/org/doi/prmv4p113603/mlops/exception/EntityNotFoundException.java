package org.doi.prmv4p113603.mlops.exception;

public abstract class EntityNotFoundException extends RuntimeException {

    // NOTE: It's abstract because this is not going to be thrown directly; always a subclass.

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
