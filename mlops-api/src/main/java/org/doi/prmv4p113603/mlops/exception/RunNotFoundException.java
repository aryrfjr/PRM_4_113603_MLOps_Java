package org.doi.prmv4p113603.mlops.exception;

public class RunNotFoundException extends EntityNotFoundException {

    public RunNotFoundException(String number) {
        super("Run with number " + number + " not found.");
    }

}
