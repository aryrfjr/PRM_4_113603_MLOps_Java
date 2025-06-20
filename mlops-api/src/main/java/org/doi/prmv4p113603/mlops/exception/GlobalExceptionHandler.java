package org.doi.prmv4p113603.mlops.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * This is a class that implements a way to apply cross-cutting concern logic
 * (like error handling) across multiple controllers without repeating code.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /*
     * NOTE: Once picked up, Spring configures it to intercept exceptions (via
     *  @ExceptionHandler methods), model attributes (via @ModelAttribute), and
     *  binding logic (via @InitBinder) for all controllers.
     *
     *  It doesn't require additional configuration, when DataIntegrityViolationException
     *  is thrown anywhere in any of the controllers, Spring will route it to the matching
     *  method in GlobalExceptionHandler.
     *
     * NOTE: Since it modularizes exception handling across controllers — a classic
     *  cross-cutting concern — and applies it declaratively, without changing the
     *  controllers themselves, it is related to the principles of AOP
     *  (Aspect-Oriented Programming), although it's not implemented using classical AOP
     *  mechanisms in Spring like proxies or @Aspect.
     *
     * TODO: Add custom exception types (UserAlreadyExistsException, etc.).
     *
     * TODO: Handle @Valid/@Validated bean validation errors.
     *
     * TODO: Return problem details using RFC 7807 (application/problem+json).
     */

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "error", "Data integrity violation",
                        "message", ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "error", "Bad request",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "error", "Internal server error",
                        "message", ex.getMessage()
                ));
    }
}
