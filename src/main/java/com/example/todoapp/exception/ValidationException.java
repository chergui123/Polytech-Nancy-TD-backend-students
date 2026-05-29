package com.example.todoapp.exception;

import com.example.todoapp.dto.ErrorDto;

import java.util.List;

/**
 * Exception thrown when DTO validation fails.
 */
public class ValidationException extends RuntimeException {
    private final List<ErrorDto> errors;

    public ValidationException(List<ErrorDto> errors) {
        this.errors = errors;
    }

    public List<ErrorDto> getErrors() {
        return errors;
    }
}
