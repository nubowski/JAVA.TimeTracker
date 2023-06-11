package ru.nubowski.timeTracker.exception;

public class CleanupFailedException extends RuntimeException{
    public CleanupFailedException() {
        super("Cleanup process failed");
    }

    public CleanupFailedException(String method) {
        super("Cleanup process failed for method: " + method);
    }
}
