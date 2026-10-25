package com.cinema.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.util.NoSuchElementException;

/** Converts exceptions thrown anywhere in the app into clean JSON error responses: {"message": "..."} */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** THE optimistic-locking case: two users raced for the same seat and we lost. */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> onRace(ObjectOptimisticLockingFailureException e) {
        return error(HttpStatus.CONFLICT, "Someone just took one of those seats. Please pick again.");
    }

    @ExceptionHandler(SeatUnavailableException.class)
    public ResponseEntity<Map<String, String>> onUnavailable(SeatUnavailableException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> onMissing(NoSuchElementException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> onInvalid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).findFirst().orElse("Invalid request");
        return error(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> onIntegrity(DataIntegrityViolationException e) {
        return error(HttpStatus.CONFLICT, "This item is still in use and cannot be changed.");
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
