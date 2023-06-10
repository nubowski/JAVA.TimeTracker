package ru.nubowski.timeTracker.exception;

public class TaskAlreadyExistsException extends RuntimeException{
    public TaskAlreadyExistsException(Long taskId) {
        super("Task with ID " + taskId + " already exists in the database");
    }
}
