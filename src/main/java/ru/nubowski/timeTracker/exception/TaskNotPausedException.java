package ru.nubowski.timeTracker.exception;

public class TaskNotPausedException extends RuntimeException{
    public TaskNotPausedException(Long id) {
        super("Task with ID: " + id + "not found in the database");
    }
}
