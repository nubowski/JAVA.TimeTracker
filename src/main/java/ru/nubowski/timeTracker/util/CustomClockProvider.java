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

    public void plusTime(Duration duration) {
        now = now.plus(duration);
    }

    public void minusTime(Duration duration) {
        now = now.minus(duration);
    }

    public void resetTime() {
        now = LocalDateTime.now();
    }

    public LocalDateTime LocalTime() {
        return LocalDateTime.now();
    }
}