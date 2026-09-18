package com.challenge.api.exception;

import java.util.UUID;

/** Translated into a 404 by {@link GlobalExceptionHandler}, which keeps the Service layer free of HTTP concerns. */
public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(UUID uuid) {
        super("No employee with uuid " + uuid);
    }
}
