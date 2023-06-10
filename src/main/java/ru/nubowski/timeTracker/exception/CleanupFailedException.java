package ru.nubowski.timeTracker.exception;

public class CleanupFailedException extends RuntimeException{
    public CleanupFailedException() {
        super("Cleanup process failed");
    }
}
