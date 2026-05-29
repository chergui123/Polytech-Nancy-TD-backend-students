package com.example.todoapp.persistence;

import com.example.todoapp.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Data Access Object for {@link Task} model using SQLite.
 */
public class TaskDao {

    private static final Logger log = LoggerFactory.getLogger(TaskDao.class);
    private static final String DB_URL = "jdbc:sqlite:todo.db";

    public TaskDao() {
        initDb();
    }

    private void initDb() {
        String sql = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT,
                    done BOOLEAN NOT NULL DEFAULT 0
                );
                """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            log.info("Database initialized.");
        } catch (SQLException e) {
            log.error("Error initializing database", e);
        }
    }

    public Task save(Task task) {
        if (task.id() == null) {
            String sql = "INSERT INTO tasks(title, description, done) VALUES(?,?,?)";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, task.title());
                pstmt.setString(2, task.description());
                pstmt.setBoolean(3, task.done());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return new Task(generatedKeys.getInt(1), task.title(), task.description(), task.done());
                    }
                }
            } catch (SQLException e) {
                log.error("Error saving new task", e);
            }
        } else {
            String sql = "UPDATE tasks SET title = ?, description = ?, done = ? WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, task.title());
                pstmt.setString(2, task.description());
                pstmt.setBoolean(3, task.done());
                pstmt.setInt(4, task.id());
                pstmt.executeUpdate();
                return task;
            } catch (SQLException e) {
                log.error("Error updating task", e);
            }
        }
        return null;
    }

    public Optional<Task> findById(int id) {
        String sql = "SELECT id, title, description, done FROM tasks WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Task(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getBoolean("done")
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding task by id", e);
        }
        return Optional.empty();
    }

    public Collection<Task> findAll() {
        Collection<Task> tasks = new ArrayList<>();
        String sql = "SELECT id, title, description, done FROM tasks";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getBoolean("done")
                ));
            }
        } catch (SQLException e) {
            log.error("Error finding all tasks", e);
        }
        return tasks;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            log.error("Error deleting task", e);
            return false;
        }
    }
}
