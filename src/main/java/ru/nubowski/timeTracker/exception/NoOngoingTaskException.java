package ru.nubowski.timeTracker.exception;

public class NoOngoingTaskException extends RuntimeException{
    public NoOngoingTaskException(Long taskId) {
        super("No ongoing task was found for the task ID " + taskId);
    }
}
