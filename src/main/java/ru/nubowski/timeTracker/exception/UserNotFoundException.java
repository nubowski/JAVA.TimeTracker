package ru.nubowski.timeTracker.exception;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String username) {
        super("User with username " + username + " not found in the database");
    }

    public UserNotFoundException(Long id) {
        super("User with username " + id + " not found in the database");
    }
}
