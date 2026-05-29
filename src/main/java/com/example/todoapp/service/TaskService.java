package com.example.todoapp.service;

import com.example.todoapp.dto.ErrorDto;
import com.example.todoapp.dto.TaskRequestDto;
import com.example.todoapp.dto.TaskResponseDto;
import com.example.todoapp.exception.ValidationException;
import com.example.todoapp.model.Task;
import com.example.todoapp.persistence.TaskDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Task management using DTOs.
 */
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    private final TaskDao dao;

    public TaskService(TaskDao dao) {
        this.dao = dao;
    }

    public Collection<TaskResponseDto> getAllTasks(boolean todoOnly) {
        log.info("Fetching all tasks (todoOnly: {})", todoOnly);
        return dao.findAll().stream()
                .filter(t -> !todoOnly || !t.done())
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public Optional<TaskResponseDto> getTaskById(int id) {
        log.info("Fetching task with id: {}", id);
        return dao.findById(id).map(this::toResponseDto);
    }

    public TaskResponseDto createTask(TaskRequestDto dto) {
        validate(dto);
        log.info("Creating new task: {}", dto.title());
        Task task = new Task(null, dto.title(), dto.description(), false);
        Task saved = dao.save(task);
        return toResponseDto(saved);
    }

    public void updateTask(int id, TaskRequestDto dto) {
        validate(dto);
        log.info("Updating task with id: {}", id);
        boolean done = dto.done() != null ? dto.done() : false;
        Task task = new Task(id, dto.title(), dto.description(), done);
        dao.save(task);
    }

    public boolean deleteTask(int id) {
        log.info("Deleting task with id: {}", id);
        return dao.delete(id);
    }

    private void validate(TaskRequestDto dto) {
        List<ErrorDto> errors = new ArrayList<>();
        if (dto.title() == null || dto.title().isBlank()) {
            errors.add(new ErrorDto("title", "Title is required"));
        } else if (dto.title().length() > 50) {
            errors.add(new ErrorDto("title", "Title must not exceed 50 characters"));
        }

        if (dto.description() != null && dto.description().length() > 255) {
            errors.add(new ErrorDto("description", "Description must not exceed 255 characters"));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private TaskResponseDto toResponseDto(Task task) {
        return new TaskResponseDto(task.id(), task.title(), task.description(), task.done());
    }
}
