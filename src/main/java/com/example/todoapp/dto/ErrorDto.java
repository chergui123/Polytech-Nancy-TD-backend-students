package com.example.todoapp.dto;

/**
 * DTO for error messages.
 * @param field       the field that caused the error
 * @param description description of the error
 */
public record ErrorDto(String field, String description) {
}
