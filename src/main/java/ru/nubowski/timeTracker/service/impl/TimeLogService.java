package ru.nubowski.timeTracker.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.exception.TaskNotFoundException;
import ru.nubowski.timeTracker.exception.TimeLogNotFoundException;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.TaskState;
import ru.nubowski.timeTracker.model.TimeLog;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.repository.TimeLogRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for managing time logs.
 */
@Service
public class TimeLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeLogService.class);
    private final TimeLogRepository timeLogRepository;

    /**
     * Constructor for the TimeLogService.
     *
     * @param timeLogRepository the time log repository
     */
    public TimeLogService(TimeLogRepository timeLogRepository) {
        this.timeLogRepository = timeLogRepository;
    }

    /**
     * Returns all time logs.
     *
     * @return a list of all time logs
     */
    public List<TimeLog> getAllTimeLogs(){
        LOGGER.debug("Getting all time logs");
        return timeLogRepository.findAll();
    }

    /**
     * Find a time log by id.
     *
     * @param id the id of the time log
     * @return the time log with the specified id
     * @throws TimeLogNotFoundException if no time log is found with the specified id
     */
    public TimeLog getTimeLog(Long id) {
        LOGGER.debug("Getting time log with id: {}", id);
        return timeLogRepository.findById(id)
                .orElseThrow(() -> new TimeLogNotFoundException(id));
    }

    /**
     * Find all time logs for a task.
     *
     * @param task the task to retrieve time logs for
     * @return a list of time logs associated with the specified task
     */
    public List<TimeLog> getAllTimeLogsForTask(Task task) {
        LOGGER.debug("Getting all time logs of the task with id: {}", task.getId());
        return timeLogRepository.findAllByTask(task);
    }

    /**
     * Saves a time log.
     *
     * @param timeLog the time log to be saved
     * @return the saved time log
     */
    public TimeLog saveTimeLog(TimeLog timeLog) {
        LOGGER.info("Saving a new time log");
        return timeLogRepository.save(timeLog);
    }

    /**
     * Deletes a time log by id.
     *
     * @param id the id of the time log to be deleted
     * @throws TimeLogNotFoundException if no time log is found with the specified id
     */
    public void deleteTimeLog(Long id) {
        LOGGER.info("Deleting time log");
        LOGGER.debug("Deleting time log with id: {}", id); // mbe this is the way?? TODO check&ask must a better way..
        if (!timeLogRepository.existsById(id)) {
            throw new TimeLogNotFoundException(id);
        }
        timeLogRepository.deleteById(id);
    }

    /**
     * Find all time logs for a user within a date range.
     *
     * @param user the user to retrieve time logs for
     * @param start the start of the date range
     * @param end the end of the date range
     * @return a list of time logs for the specified user within the specified date range
     */
    public List<TimeLog> getTimeLogsByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end) {
        LOGGER.debug("Getting time logs for user {} between {} and {}", user.getUsername(), start, end);
        return timeLogRepository.findTST(user, start, end);
    }

    /**
     * Deletes all time logs for a task.
     *
     * @param task the task to delete time logs for
     */
    public void deleteTimeLogsByTask(Task task) {
        LOGGER.info("Deleting time logs for task: {}", task.getName());
        List<TimeLog> timeLogs = timeLogRepository.findByTask(task);
        timeLogRepository.deleteAll(timeLogs); // believed in the IDE and replace with the bulk... (!)
    }

    /**
     * Returns the total work effort for a user within a date range.
     * Calculated on the BD side with the native sql query request
     *
     * @param user the user to calculate work effort for
     * @param start the start of the date range
     * @param end the end of the date range
     * @return the total work effort for the specified user within the specified date range
     */
    public Duration getTotalWorkEffortByUserAndDataRange(User user, LocalDateTime start, LocalDateTime end) {
        LOGGER.info("Getting total work effort for username {} id {} between {} and {}", user.getUsername(), user.getId(), start, end);
        return Duration.ofSeconds(timeLogRepository.getTotalWorkEffortInSeconds(user.getId(), start, end));
    }

    /**
     * Returns the duration of a task within a date range.
     * DEPRECATED
     *
     * @param timeLog the time log associated with the task
     * @param start the start of the date range
     * @param end the end of the date range
     * @return the duration of the task within the specified date range
     */
    private Duration getTaskDuration(TimeLog timeLog, LocalDateTime start, LocalDateTime end) {
        LOGGER.info("Getting task duration for {} with the start {} and  end {} name of {}", timeLog, timeLog.getStartTime(), timeLog.getEndTime(), timeLog.getTask().getName());
        LocalDateTime startTime = timeLog.getStartTime().isBefore(start) ? start : timeLog.getStartTime();
        LocalDateTime endTime = timeLog.getEndTime() != null ? timeLog.getEndTime() : end;
        // if startTime after end or endTime before start, return zero
        if (startTime.isAfter(end) || endTime.isBefore(start)) {
            return Duration.ZERO;
        }
        // if endTime is after end, set it to end
        if (endTime.isAfter(end)) {
            endTime = end;
        }
        return Duration.between(startTime, endTime);
    }

    /**
     * Automatically ends ongoing tasks at a scheduled time.
     */
    @Scheduled(cron = "0 59 23 * * ?") // TODO: make a schedule or whatever package
    public void autoEndTasks() {
        LOGGER.info("Auto-ending ongoing tasks");
        List<TimeLog> ongoingTimeLog = timeLogRepository.findByEndTimeIsNullAndTaskStateEquals(TaskState.ONGOING); // PAUSE
        ongoingTimeLog.forEach(timeLog -> {
            timeLog.setEndTime(LocalDateTime.now()); // TODO: LocalDateTime out of loop --^ condition ONGOING on each + bulk
            timeLog.setTaskState(TaskState.AUTO_STOPPED);
            timeLogRepository.save(timeLog);
        });
    }

    /**
     * Deletes old time logs before a cutoff date.
     *
     * @param cutoff the cutoff date
     */
    public void deleteOldTimeLogs(LocalDateTime cutoff) {
        LOGGER.info("Deleting old time logs before {}", cutoff);
        List<TimeLog> oldTimeLogs = timeLogRepository.findByStartTimeBefore(cutoff);
        timeLogRepository.deleteAll(oldTimeLogs);
    }

    /**
     * Returns the last time log for a task.
     *
     * @param task the task to retrieve the last time log for
     * @return the last time log for the specified task
     * @throws TimeLogNotFoundException if no time log is found for the specified task
     */
    public TimeLog getLastTimeLogForTask(Task task) {
        LOGGER.debug("Getting the last time log for task with id: {}", task.getId());
        return timeLogRepository.findFirstByTaskOrderByStartTimeDesc(task)
                .orElseThrow(() -> new TimeLogNotFoundException(task.getId()));
    }

    /**
     * Returns the total time elapsed for a task.
     *
     * @param task the task to calculate total time elapsed for
     * @return the total time elapsed for the specified task
     * @throws TaskNotFoundException if no time logs are found for the specified task
     */
    public Duration getTaskTimeElapsed(Task task) { // TODO: old logs + ongoing
        LOGGER.debug("Getting time elapsed for task with id: {}", task.getId());
        List<TimeLog> timeLogs = timeLogRepository.findByTaskOrderByStartTimeAsc(task);
        if(timeLogs.isEmpty()) {
            throw new TaskNotFoundException(task.getId());
        }
        Duration totalDuration = Duration.ZERO;
        for(TimeLog timeLog : timeLogs) {
            LocalDateTime end = (timeLog.getEndTime() != null) ? timeLog.getEndTime() : LocalDateTime.now();
            Duration duration = Duration.between(timeLog.getStartTime(), end);
            totalDuration = totalDuration.plus(duration);
        }
        return totalDuration;
    }

    /**
     * Sorts time logs according to the specified sort order and returns the sorted logs along with their associated tasks and durations.
     *
     * @param timeLogs the time logs to be sorted
     * @param sort the sort order
     * @param end the end of the date range
     * @return a list of entries where each entry consists of a task and a duration
     */
    public List<Map.Entry<Task, Long>> sortTimeLogs(List<TimeLog> timeLogs, String sort, LocalDateTime end) {
        if (sort.equals("start_time")) {
            timeLogs.sort(Comparator.comparing(TimeLog::getStartTime));
        }
        Map<Task, Long> durationsPerTask = timeLogs.stream()
                .collect(Collectors.groupingBy(
                        TimeLog::getTask,
                        Collectors.summingLong(log -> Duration.between(log.getStartTime(), log.getEndTime() != null ? log.getEndTime() : end.isBefore(LocalDateTime.now()) ? end : LocalDateTime.now()).toMillis())
                ));
        Stream<Map.Entry<Task, Long>> stream = durationsPerTask.entrySet().stream();
        if (sort.equals("duration")) {
            stream = stream.sorted(Map.Entry.<Task, Long>comparingByValue().reversed());
        }
        return stream.collect(Collectors.toList());
    }

    /**
     * Formats time logs according to the specified output format and returns the formatted logs.
     *
     * @param timeLogs the time logs to be formatted
     * @param sortedTimeLogs the sorted time logs along with their associated tasks and durations
     * @param output the output format
     * @return a list of formatted time logs
     * @throws IllegalArgumentException if the specified output format is unexpected
     */
    public List<String> formatTimeLogs(List<TimeLog> timeLogs, List<Map.Entry<Task, Long>> sortedTimeLogs, String output) {
        if (output.equals("duration")) {
            return sortedTimeLogs.stream()
                    .map(entry -> {
                        Task task = entry.getKey();
                        Duration duration = Duration.ofMillis(entry.getValue());
                        return String.format("%s - %02d:%02d",
                                task.getName(),
                                duration.toHours(),
                                duration.toMinutesPart());
                    })
                    .collect(Collectors.toList());
        }
        if (output.equals("interval")) {
            return timeLogs.stream()
                    .map(log -> String.format("%s - %s | %s",
                            log.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            log.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            log.getTask().getName()))
                    .collect(Collectors.toList());
        }
        throw new IllegalArgumentException("Unexpected output value: " + output);
    }

}
