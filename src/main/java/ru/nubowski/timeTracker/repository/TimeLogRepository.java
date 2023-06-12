package ru.nubowski.timeTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.TaskState;
import ru.nubowski.timeTracker.model.TimeLog;
import ru.nubowski.timeTracker.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TimeLogRepository extends JpaRepository <TimeLog, Long> {
    Optional<TimeLog> findFirstByTaskAndEndTimeIsNullOrderByStartTimeDesc (Task task); // What The Hell ??? Is THAT naming (done by convention xDD)
    List<TimeLog> findByEndTimeIsNull(); // DEPRECATED
    List<TimeLog> findTimeLogByTaskUserAndStartTimeAfterAndEndTimeBefore(User user, LocalDateTime start, LocalDateTime end);
    List<TimeLog> findByEndTimeIsNullAndTaskStateEquals(TaskState taskState); // -> LIST
    List<TimeLog> findByTaskUser(User user);
    List<TimeLog> findByTaskOrderByStartTimeAsc(Task task);
    List<TimeLog> findByStartTimeBefore(LocalDateTime cutoff);
    Optional<TimeLog> findFirstByTaskAndTaskStateOrderByStartTimeDesc(Task task, TaskState taskState);
    Optional<TimeLog> findFirstByTaskOrderByStartTimeDesc(Task task); // just in case of last log as is

    List<TimeLog> findAllByTask(Task task);

    List<TimeLog> findByTaskOrderByStartTime (Task task); // DEPRECATED

    // method to fetch TimeLogs by user and data range TODO: too complex and unreadable. To think
    @Query("SELECT t FROM TimeLog t WHERE t.task.user = :user AND ((t.startTime BETWEEN :start AND :end) OR (t.endTime BETWEEN :start AND :end))")
    List<TimeLog> findByUserAndDateRange(@Param("user") User user, @Param("start")LocalDateTime start, @Param("end") LocalDateTime end);
}
