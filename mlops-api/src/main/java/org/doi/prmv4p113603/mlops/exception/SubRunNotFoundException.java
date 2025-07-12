package org.doi.prmv4p113603.mlops.exception;

public class SubRunNotFoundException extends EntityNotFoundException {

    public SubRunNotFoundException(String number) {
        super("SubRun with number " + number + " not found.");
    }

}
