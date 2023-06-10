package ru.nubowski.timeTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.TimeLog;

import java.util.Optional;

public interface TimeLogRepository extends JpaRepository <TimeLog, Long> {
    Optional<TimeLog> findFirstByTaskAndEndTimeIsNullOrderByStartTimeDesc (Task task); // What The Hell ??? Is THAT naming (done by convention xDD)
}
