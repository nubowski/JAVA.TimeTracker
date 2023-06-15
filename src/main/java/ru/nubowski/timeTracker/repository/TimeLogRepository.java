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

    List<TimeLog> findByEndTimeIsNullAndTaskStateEquals(TaskState taskState); // -> LIST
    @Query("SELECT t FROM TimeLog t WHERE t.task.user = :user AND ((t.startTime < :end AND t.endTime > :start) OR (t.endTime is NULL AND t.startTime < :end))")
    List<TimeLog> findTST (User user, LocalDateTime start, LocalDateTime end);


    Optional<TimeLog> findByTaskAndTaskState(Task task, TaskState taskState);

    List<TimeLog> findByTaskUser(User user);
    List<TimeLog> findByTaskOrderByStartTimeAsc(Task task);
    List<TimeLog> findByStartTimeBefore(LocalDateTime cutoff);

    Optional<TimeLog> findFirstByTaskOrderByStartTimeDesc(Task task); // just in case of last log as is

    List<TimeLog> findByTask(Task task);

    List<TimeLog> findAllByTask(Task task);

    List<TimeLog> findByTaskOrderByStartTime (Task task); // DEPRECATED

    // method to fetch TimeLogs by user and data range TODO: too complex and unreadable. But MUCH faster and independent of traffic
    @Query(value = "SELECT SUM(EXTRACT(EPOCH FROM (" +
            "LEAST(COALESCE(t.end_time, CURRENT_TIMESTAMP), :end) - " +
            "GREATEST(t.start_time, :start)))) " +
            "FROM time_logs t " +
            "INNER JOIN tasks task ON t.task_id = task.id " +
            "WHERE task.user_id = :user_id AND ((t.start_time < :end AND t.end_time > :start) OR (t.end_time IS NULL AND t.start_time < :end)) " +
            "HAVING SUM(EXTRACT(EPOCH FROM (" +
            "LEAST(COALESCE(t.end_time, CURRENT_TIMESTAMP), :end) - " +
            "GREATEST(t.start_time, :start)))) > 0", nativeQuery = true)
    Long getTotalWorkEffortInSeconds(@Param("user_id") Long id, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


}
