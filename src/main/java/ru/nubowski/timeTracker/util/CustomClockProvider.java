package ru.nubowski.timeTracker.util;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Profile("test")
@Service
public class CustomClockProvider implements ClockProvider {
    private LocalDateTime now = LocalDateTime.now();
    @Override
    public LocalDateTime now() {
        return now;
    }

    public void advancedBy(Duration duration) {
        now = now.plus(duration);
    }
}
