package com.challenge.api.exception;

/**
 * Translated into a 400 by {@link GlobalExceptionHandler}. Unlike the same rule on create, this one needs the stored
 * hire date, so it cannot be a validation annotation on the request.
 */
public class InvalidTerminationDateException extends RuntimeException {

    public InvalidTerminationDateException() {
        super("contractTerminationDate must not be before contractHireDate");
    }
}
