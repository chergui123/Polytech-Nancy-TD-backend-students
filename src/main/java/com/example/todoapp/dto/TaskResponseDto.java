package com.example.todoapp.dto;

/**
 * DTO for task responses.
 * @param id            task identifier
 * @param title         task title
 * @param description   task description
 * @param done          task status
 */
public record TaskResponseDto(Integer id, String title, String description, boolean done) {
}
