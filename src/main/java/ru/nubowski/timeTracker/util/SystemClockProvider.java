package ru.nubowski.timeTracker.util;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Profile("!test")
@Service
public class SystemClockProvider implements ClockProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

}
