// GlobalExceptionHandler.java
// Production-ready global exception handler
package com.example.api;

import com.example.api.exception.BusinessException;
import com.example.api.exception.ErrorResponse;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.exception.ValidationException;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MeterRegistry meterRegistry;

    // Include stack traces in error responses only in non-production environments
    @Value("${app.include-error-details:false}")
    private boolean includeErrorDetails;

    public GlobalExceptionHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // Handle custom business exceptions
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {

        String traceId = getOrCreateTraceId();

        log.warn("Business exception [{}]: {} - {}",
                traceId, ex.getErrorCode(), ex.getMessage());

        incrementErrorCounter(ex.getErrorCode(), ex.getStatus().value());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(ex.getStatus().value())
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .path(extractPath(request))
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(error, ex.getStatus());
    }

    // Handle resource not found with extra context
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {

        String traceId = getOrCreateTraceId();

        log.info("Resource not found [{}]: {} with id {}",
                traceId, ex.getResourceType(), ex.getResourceId());

        incrementErrorCounter("RESOURCE_NOT_FOUND", 404);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .path(extractPath(request))
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // Handle validation exceptions with field-level errors
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {

        String traceId = getOrCreateTraceId();

        log.info("Validation failed [{}]: {}", traceId, ex.getError());

        incrementErrorCounter("VALIDATION_ERROR", 400);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("Validation failed")
                .path(extractPath(request))
                .traceId(traceId)
                .validationErrors(ex.getError())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Handle Spring's @Valid annotation failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {

        String traceId = getOrCreateTraceId();

        // Extract field-level validation errors
        Map<String, List<String>> validationErrors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors
                    .computeIfAbsent(fieldError.getField(), k -> new ArrayList<>())
                    .add(fieldError.getDefaultMessage());
        }

        log.info("Request validation failed [{}]: {}", traceId, validationErrors);

        incrementErrorCounter("VALIDATION_ERROR", 400);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("Request validation failed")
                .path(extractPath(request))
                .traceId(traceId)
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Handle constraint violations (path/query parameter validation)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        String traceId = getOrCreateTraceId();

        Map<String, List<String>> validationErrors = new HashMap<>();

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String field = violation.getPropertyPath().toString();
            validationErrors
                    .computeIfAbsent(field, k -> new ArrayList<>())
                    .add(violation.getMessage());
        }

        log.info("Constraint violation [{}]: {}", traceId, validationErrors);

        incrementErrorCounter("VALIDATION_ERROR", 400);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("Parameter validation failed")
                .path(extractPath(request))
                .traceId(traceId)
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Handle malformed JSON requests
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {

        String traceId = getOrCreateTraceId();

        log.warn("Malformed request body [{}]: {}", traceId, ex.getMessage());

        incrementErrorCounter("MALFORMED_REQUEST", 400);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("MALFORMED_REQUEST")
                .message("Request body is malformed or missing")
                .path(extractPath(request))
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Handle missing required request parameters
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex, WebRequest request) {

        String traceId = getOrCreateTraceId();

        log.info("Missing parameter [{}]: {}", traceId, ex.getParameterName());

        incrementErrorCounter("MISSING_PARAMETER", 400);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("MISSING_PARAMETER")
                .message(String.format("Required parameter '%s' is missing", ex.getParameterName()))
                .path(extractPath(request))
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Handle type mismatches (e.g., string instead of number)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        String traceId = getOrCreateTraceId();

        String expectedType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "unknown";

        log.info("Type mismatch [{}]: parameter {} expected {}", traceId, ex.getName(), expectedType);

        incrementErrorCounter("TYPE_MISMATCH", 400);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("TYPE_MISMATCH")
                .message(String.format("Parameter '%s' should be of type %s", ex.getName(), expectedType))
                .path(extractPath(request))
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Handle unsupported HTTP methods
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {

        String traceId = getOrCreateTraceId();

        incrementErrorCounter("METHOD_NOT_ALLOWED", 405);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("METHOD_NOT_ALLOWED")
                .message(String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod()))
                .path(extractPath(request))
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // Handle unsupported media types
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {

        String traceId = getOrCreateTraceId();

        incrementErrorCounter("UNSUPPORTED_MEDIA_TYPE", 415);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .error("UNSUPPORTED_MEDIA_TYPE")
                .message(String.format("Content type '%s' is not supported", ex.getContentType()))
                .path(extractPath(request))
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // Handle 404 for undefined endpoints
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, WebRequest request) {

        String traceId = getOrCreateTraceId();

        incrementErrorCounter("ENDPOINT_NOT_FOUND", 404);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("ENDPOINT_NOT_FOUND")
                .message(String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL()))
                .path(extractPath(request))
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // Catch-all for unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex, WebRequest request) {

        String traceId = getOrCreateTraceId();

        // Log full stack trace for debugging
        log.error("Unhandled exception [{}]: {}", traceId, ex.getMessage(), ex);

        incrementErrorCounter("INTERNAL_ERROR", 500);

        ErrorResponse.ErrorResponseBuilder builder = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .path(extractPath(request))
                .traceId(traceId);

        // Include exception details only in non-production environments
        if (includeErrorDetails) {
            builder.details(Map.of(
                    "exception", ex.getClass().getName(),
                    "message", ex.getMessage()
            ));
        }

        return new ResponseEntity<>(builder.build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Get trace ID from MDC (set by tracing infrastructure) or generate one
    private String getOrCreateTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        return traceId;
    }

    // Extract path from WebRequest
    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    // Track error metrics for monitoring
    private void incrementErrorCounter(String errorCode, int statusCode) {
        meterRegistry.counter("api_errors_total",
                "error_code", errorCode,
                "status_code", String.valueOf(statusCode)
        ).increment();
    }
}