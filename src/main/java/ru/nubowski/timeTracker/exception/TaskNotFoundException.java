package ru.nubowski.timeTracker.exception;

public class TaskNotFoundException extends RuntimeException{
    public TaskNotFoundException(Long id) {
        super("Task not found with ID: " + id);
    }
}
