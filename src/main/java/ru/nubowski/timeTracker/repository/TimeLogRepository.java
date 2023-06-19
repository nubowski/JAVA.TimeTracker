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

/**
 * TimeLogRepository is an interface for performing database operations on {@link TimeLog} entities.
 * It extends Spring Data JPA {@link JpaRepository}, gaining methods like save(), findAll(), and findById().
 */
public interface TimeLogRepository extends JpaRepository <TimeLog, Long> {
    /**
     * Finds the most recent time log for the specified task that has not yet ended.
     * @param task The task for which to find the time log.
     * @return An optional time log that matches these criteria.
     */
    Optional<TimeLog> findFirstByTaskAndEndTimeIsNullOrderByStartTimeDesc (Task task);

    /**
     * Finds time logs where the end time is null (meaning the task is ongoing) and the task state matches the specified value.
     * @param taskState The state of the task.
     * @return A list of time logs that matches these criteria.
     */
    List<TimeLog> findByEndTimeIsNullAndTaskStateEquals(TaskState taskState); // -> LIST

    /**
     * Finds time logs for a specific task and task state.
     * @param task The task for which to find the time logs.
     * @param taskState The state of the task.
     * @return An OPTIONAL time log that matches these criteria.
     */
    Optional<TimeLog> findByTaskAndTaskState(Task task, TaskState taskState);

    /**
     * Finds the most recent time log for the specified task.
     * @param task The task for which to find the time log.
     * @return A time log that matches these criteria.
     */
    List<TimeLog> findByTaskOrderByStartTimeAsc(Task task);

    /**
     * Finds time logs where the start time is before the specified cutoff.
     * @param cutoff The cutoff time.
     * @return A list of time logs that matches these criteria.
     */
    List<TimeLog> findByStartTimeBefore(LocalDateTime cutoff);

    /**
     * Finds the most recent time log for the specified task.
     * @param task The task for which to find the time log.
     * @return An OPTIONAL time log that matches these criteria.
     */
    Optional<TimeLog> findFirstByTaskOrderByStartTimeDesc(Task task); // just in case of last log as is

    /**
     * Finds time logs for a specific task.
     * @param task The task for which to find the time logs.
     * @return A list of time logs that matches these criteria.
     */
    List<TimeLog> findByTask(Task task);

    /**
     * Finds all time logs for a specific task.
     * @param task The task for which to find the time logs.
     * @return A list of time logs that matches the specified criteria.
     */
    List<TimeLog> findAllByTask(Task task);



    /**
     * Deprecated method. Please refer to {@link #getTotalWorkEffortInSeconds} instead.
     */
    @Query("SELECT t FROM TimeLog t WHERE t.task.user = :user AND ((t.startTime < :end AND t.endTime > :start) OR (t.endTime is NULL AND t.startTime < :end))")
    List<TimeLog> findTST (User user, LocalDateTime start, LocalDateTime end);

    // TODO: too complex and unreadable for newbies (like me). But MUCH faster and independent of traffic

    /**
     * Fetches the total work effort in seconds by a user within a specified time range. This query uses native SQL for performance and data transfer reason reasons ^-^
     *
     * @param id The id of the user.
     * @param start The start of the time range.
     * @param end The end of the time range.
     * @return The total work effort in seconds.
     */
    @Query(value = "SELECT SUM(EXTRACT(EPOCH FROM (LEAST(COALESCE(t.end_time, CURRENT_TIMESTAMP), :end) - GREATEST(t.start_time, :start)))) " +
            "FROM time_logs t " +
            "INNER JOIN tasks task ON t.task_id = task.id " +
            "WHERE task.user_id = :user_id AND ((t.start_time < :end AND t.end_time > :start) OR (t.end_time IS NULL AND t.start_time < :end)) " +
            "HAVING SUM(EXTRACT(EPOCH FROM (LEAST(COALESCE(t.end_time, CURRENT_TIMESTAMP), :end) - GREATEST(t.start_time, :start)))) > 0",
            nativeQuery = true)
    Long getTotalWorkEffortInSeconds(@Param("user_id") Long id, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}
