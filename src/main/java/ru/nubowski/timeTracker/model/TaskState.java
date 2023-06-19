package ru.nubowski.timeTracker.model;

/**
 * Represents the state of a task in the time tracking system.
 * The task state can be one of the following:
 * ONGOING: The task is currently in progress.
 * PAUSED: The task has been paused.
 * USER_STOPPED: The task was manually stopped by the user.
 * AUTO_STOPPED: The task was automatically stopped.
 * UNEXPECTEDLY_STOPPED: The task was stopped unexpectedly.
 */
public enum TaskState {
    ONGOING,
    PAUSED,
    USER_STOPPED,
    AUTO_STOPPED,
    UNEXPECTEDLY_STOPPED
}
