package org.doi.prmv4p113603.mlops.exception;

public abstract class InsertionNotAllowedException extends RuntimeException {

    // NOTE: It's abstract because this is not going to be thrown directly; always a subclass.

    public InsertionNotAllowedException(String message) {
        super(message);
    }

    public InsertionNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

}
