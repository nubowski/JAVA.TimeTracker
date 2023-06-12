package ru.nubowski.timeTracker.util;

import java.time.LocalDateTime;

public interface ClockProvider {
    LocalDateTime now();
    // ..
}
