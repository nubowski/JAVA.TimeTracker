package ru.nubowski.timeTracker.util;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * A clock provider that returns the system's current time.
 */
@Profile("!test")
@Service
public class SystemClockProvider implements ClockProvider {

    /**
     * Gets the system's current time.
     *
     * @return the system's current time.
     */
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

}
