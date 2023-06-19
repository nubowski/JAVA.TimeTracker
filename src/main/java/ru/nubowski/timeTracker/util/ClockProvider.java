package ru.nubowski.timeTracker.util;


import java.time.LocalDateTime;

/**
 * An interface for providing the current time.
 */
public interface ClockProvider {
    /**
     * Gets the current time.
     *
     * @return the current time.
     */
    LocalDateTime now();
    // ..
}
