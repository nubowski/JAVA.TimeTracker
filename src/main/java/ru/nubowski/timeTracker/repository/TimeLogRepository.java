package ru.nubowski.timeTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nubowski.timeTracker.model.TimeLog;

public interface TimeLogRepository extends JpaRepository <TimeLog, Long> {
}
