package ru.nubowski.timeTracker.util;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * A custom clock provider for testing. Allows manipulation of the returned time.
 */
@Profile("test")
@Service
public class CustomClockProvider implements ClockProvider {
    private LocalDateTime now = LocalDateTime.now();

    /**
     * Gets the current time as defined in this custom clock.
     *
     * @return the current time.
     */
    @Override
    public LocalDateTime now() {
        return now;
    }

    /**
     * Adds the given duration to the current time.
     *
     * @param duration the duration to add.
     */
    public void plusTime(Duration duration) {
        now = now.plus(duration);
    }

    /**
     * Subtracts the given duration from the current time.
     *
     * @param duration the duration to subtract.
     */
    public void minusTime(Duration duration) {
        now = now.minus(duration);
    }

    /**
     * Resets the current time to the actual current time.
     */
    public void resetTime() {
        now = LocalDateTime.now();
    }

    /**
     * Gets the actual current local date and time.
     *
     * @return the current local date and time.
     */
    public LocalDateTime LocalTime() {
        return LocalDateTime.now();
    }
}