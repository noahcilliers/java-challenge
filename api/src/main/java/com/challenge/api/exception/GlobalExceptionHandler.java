package com.challenge.api.exception;

import java.util.Map;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Single place where exceptions become HTTP responses, so every error has the same RFC 7807 body and is logged once.
 * Extending {@link ResponseEntityExceptionHandler} keeps Spring's own statuses (400, 405, 415, ...) intact; without it
 * the catch-all below would turn them all into 500s.
 *
 * <p>Neither responses nor logs repeat values supplied by the caller: they may be personal data.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ProblemDetail handleEmployeeNotFound(EmployeeNotFoundException exception) {
        log.warn("Request rejected: status=404 cause={}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    /** Anything unforeseen: full stack trace to the log, nothing about internals to the caller. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception) {
        log.error("Unexpected error while handling request", exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    /** Adds an "errors" object naming each invalid attribute, so a caller can fix every problem in one attempt. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new TreeMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.merge(
                    fieldError.getField(), fieldError.getDefaultMessage(), (first, second) -> first + "; " + second);
        }
        log.warn("Validation failed on attributes {}", errors.keySet());
        ProblemDetail body = exception.getBody();
        body.setProperty("errors", errors);
        return handleExceptionInternal(exception, body, headers, status, request);
    }

    /** Every exception Spring handles itself (malformed JSON, invalid UUID, wrong verb, ...) passes through here. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        log.warn(
                "Request rejected: status={} cause={}",
                statusCode.value(),
                exception.getClass().getSimpleName());
        return super.handleExceptionInternal(exception, body, headers, statusCode, request);
    }
}
