package com.example.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IdempotencyException.class)
    public ResponseEntity<String> handleIdempotencyException(IdempotencyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    // Handles duplicate email validation errors cleanly
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex, HttpServletRequest request) {

        // Structured error response payload schema
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", LocalDateTime.now().toString());
        errorBody.put("status", HttpStatus.CONFLICT.value()); // HTTP 409 Conflict
        errorBody.put("error", "Conflict");
        errorBody.put("message", ex.getMessage());
        errorBody.put("path", request.getRequestURI());

        return new ResponseEntity<>(errorBody, HttpStatus.CONFLICT);
    }

    // Add this handler method inside the class
    @ExceptionHandler(SerializationException.class)
    public ResponseEntity<Object> handleSerializationException(SerializationException ex, jakarta.servlet.http.HttpServletRequest request) {
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", java.time.LocalDateTime.now().toString());
        errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorBody.put("error", "Serialization Failure");
        errorBody.put("message", "Entity processing state mismatch. Relational properties could not be mapped to cache.");
        errorBody.put("path", request.getRequestURI());

        return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException ex, jakarta.servlet.http.HttpServletRequest request) {

        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", java.time.LocalDateTime.now().toString());
        errorBody.put("status", HttpStatus.NOT_FOUND.value()); // HTTP 404 Not Found
        errorBody.put("error", "Not Found");
        errorBody.put("message", ex.getMessage());
        errorBody.put("path", request.getRequestURI());

        return new ResponseEntity<>(errorBody, HttpStatus.NOT_FOUND);
    }
}
