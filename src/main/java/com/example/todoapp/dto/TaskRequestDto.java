package com.example.todoapp.dto;

/**
 * DTO for creating or updating a task.
 * @param title         task title (max 50 chars)
 * @param description   task description (max 255 chars)
 * @param done          task status (used for PUT)
 */
public record TaskRequestDto(String title, String description, Boolean done) {
}
