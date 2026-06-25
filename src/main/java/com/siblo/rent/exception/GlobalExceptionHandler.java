package com.siblo.rent.exception;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(SlotNotAvailableException.class)
    public ResponseEntity<?> handleSlotNotAvailable(SlotNotAvailableException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(BookingException.class)
    public ResponseEntity<?> handleBooking(BookingException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuth(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleMissingBody(HttpMessageNotReadableException e) {
        String message = "Request body required with action: cancel or reschedule";
        if (e.getCause() instanceof MismatchedInputException mie) {
            message = "Invalid request format: " + mie.getPathReference();
        }
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<?> handleOptimisticLock(OptimisticLockException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "One or more slots were just booked"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}
