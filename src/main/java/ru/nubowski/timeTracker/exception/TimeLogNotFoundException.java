package ru.nubowski.timeTracker.exception;

public class TimeLogNotFoundException extends RuntimeException{
    public TimeLogNotFoundException(Long id) {
        super("TimeLog with ID: " + id + " not found");
    }
}
