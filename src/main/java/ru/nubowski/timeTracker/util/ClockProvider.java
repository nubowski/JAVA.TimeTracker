package ru.nubowski.timeTracker.util;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

public interface ClockProvider {
    LocalDateTime now();
    // ..
}