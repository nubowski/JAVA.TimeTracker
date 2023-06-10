package ru.nubowski.timeTracker.exception;

public class OngoingTaskNotFoundException extends RuntimeException{
    public OngoingTaskNotFoundException(Long taskId) {
        super("Ongoing task with ID " + taskId + " not found. The task has not been started yet");
    }
}
