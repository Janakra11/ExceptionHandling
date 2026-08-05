package com.example.api.exception;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class ValidationException extends BusinessException{

    private final Map<String, List<String>> error;

    public ValidationException(Map<String, List<String>> error){
        super(
                "Validation failed",
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR"
        );
        this.error = error;
    }


    public Map<String, List<String>> getError() {
        return error;
    }
}
