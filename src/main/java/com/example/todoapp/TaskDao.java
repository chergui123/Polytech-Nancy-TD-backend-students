package com.example.todoapp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TaskDao {

    private final Map<Integer, Task> storage = new HashMap<>();

    {
        save(new Task(1, "Réviser DS de maths", "Séries numériques et probabilités.", false));
        save(new Task(2, "Valider mon PIVE", "PIVE Club Poker.", true));
        save(new Task(3, "Choisir mon parcours de 4A", "SIR ou SIA ?", false));
    }

    public Task save(Task task) {
        storage.put(task.id(), task);
        return task;
    }

    public Optional<Task> findById(int id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Task> findAll(Boolean todoOnly) {
        return storage.values().stream()
                .filter(task -> todoOnly == null || !todoOnly || !task.done())
                .toList();
    }

    public boolean deleteById(int id) {
        return storage.remove(id) != null;
    }

    public boolean update(int id, Task task) {
        if (!storage.containsKey(id)) {
            return false;
        }
        storage.put(id, new Task(id, task.title(), task.description(), task.done()));
        return true;
    }
}