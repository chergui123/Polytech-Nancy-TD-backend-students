package com.example.todoapp.controller;

import com.example.todoapp.dto.TaskRequestDto;
import com.example.todoapp.dto.TaskResponseDto;
import com.example.todoapp.exception.ValidationException;
import com.example.todoapp.model.JsonUtils;
import com.example.todoapp.persistence.TaskDao;
import com.example.todoapp.service.TaskService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

/**
 * Main class of the application. Managing routing and HTTP layer.
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");
    private static final TaskService service = new TaskService(new TaskDao());

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/tasks", Application::handleTasks);
        server.setExecutor(null);
        server.start();
        log.info("HTTP server started on http://localhost:8080");
    }

    private static void handleTasks(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        try {
            // GET /tasks
            if ("GET".equals(method) && "/tasks".equals(path)) {
                boolean todoOnly = nonNull(query) && query.contains("todo-only=true");
                Collection<TaskResponseDto> tasks = service.getAllTasks(todoOnly);

                if (tasks.isEmpty()) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 200, JsonUtils.serialize(tasks));
                }
                return;
            }

            // POST /tasks
            if ("POST".equals(method) && "/tasks".equals(path)) {
                TaskRequestDto input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), TaskRequestDto.class);
                TaskResponseDto createdTask = service.createTask(input);

                exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
                sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
                return;
            }

            // Endpoints with {id}
            Matcher m = ID_PATH.matcher(path);
            if (m.matches()) {
                int id = Integer.parseInt(m.group(1));

                // GET /tasks/{id}
                if ("GET".equals(method)) {
                    Optional<TaskResponseDto> task = service.getTaskById(id);
                    if (task.isPresent()) {
                        sendResponse(exchange, 200, JsonUtils.serialize(task.get()));
                    } else {
                        sendResponse(exchange, 404, null);
                    }
                    return;
                }

                // DELETE /tasks/{id}
                if ("DELETE".equals(method)) {
                    boolean deleted = service.deleteTask(id);
                    if (deleted) {
                        sendResponse(exchange, 204, null);
                    } else {
                        sendResponse(exchange, 404, null);
                    }
                    return;
                }

                // PUT /tasks/{id}
                if ("PUT".equals(method)) {
                    Optional<TaskResponseDto> existing = service.getTaskById(id);
                    if (existing.isPresent()) {
                        TaskRequestDto input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), TaskRequestDto.class);
                        service.updateTask(id, input);
                        sendResponse(exchange, 204, null);
                    } else {
                        sendResponse(exchange, 404, null);
                    }
                    return;
                }
            }

            sendResponse(exchange, 404, null);
        } catch (ValidationException e) {
            log.warn("Validation error: {}", e.getErrors());
            sendResponse(exchange, 400, JsonUtils.serialize(e.getErrors()));
        } catch (Exception e) {
            log.error("Unexpected error", e);
            sendResponse(exchange, 500, null);
        }
    }

    private static void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        if (nonNull(json)) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = json.getBytes(UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        }
    }
}
