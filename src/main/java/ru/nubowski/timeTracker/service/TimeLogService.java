package ru.nubowski.timeTracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.exception.OngoingTaskNotFoundException;
import ru.nubowski.timeTracker.exception.TimeLogNotFoundException;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.TimeLog;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.repository.TimeLogRepository;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimeLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeLogService.class);
    private final TimeLogRepository timeLogRepository;

    public TimeLogService(TimeLogRepository timeLogRepository) {
        this.timeLogRepository = timeLogRepository;
    }

    public List<TimeLog> getAllTimeLogs(){
        LOGGER.debug("Getting all time logs");
        return timeLogRepository.findAll();
    }

    public TimeLog getTimeLog(Long id) {
        LOGGER.debug("Getting time log with id: {}", id);
        return timeLogRepository.findById(id)
                .orElseThrow(() -> new TimeLogNotFoundException(id));
    }

    public TimeLog saveTimeLog(TimeLog timeLog) {
        LOGGER.info("Saving a new time log");
        return timeLogRepository.save(timeLog);
    }

    public void deleteTimeLog(Long id) {
        LOGGER.info("Deleting time log");
        LOGGER.debug("Deleting time log with id: {}", id); // mbe this is the way?? TODO check&ask must a better way..
        if (!timeLogRepository.existsById(id)) {
            throw new TimeLogNotFoundException(id);
        }
        timeLogRepository.deleteById(id);
    }

    public TimeLog startTask (Task task) {
        LOGGER.info("Starting a task: {}", task.getId()); // TODO info = name, debug = low level (id, hash, etc:?)
        TimeLog timeLog = new TimeLog();
        timeLog.setTask(task);
        timeLog.setStartTime(LocalDateTime.now());
        return timeLogRepository.save(timeLog);
    }

    public TimeLog stopTask(Task task) {
        LOGGER.info("Stopping task: {}", task.getId());
        TimeLog timeLog = timeLogRepository.findFirstByTaskAndEndTimeIsNullOrderByStartTimeDesc(task). // perfect naming ^-^
                orElseThrow(() -> new OngoingTaskNotFoundException(task.getId()));
        timeLog.setEndTime(LocalDateTime.now());
        timeLog.setEndedByUser(true);
        return timeLogRepository.save(timeLog);
    }

    public List<TimeLog> getTimeLogsByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end) {
        LOGGER.debug("Getting time logs for user {} between {} and {}", user.getUsername(), start, end); // we still have userID
        return timeLogRepository.findByUserAndDateRange(user, start, end);
    }

    //  delete time logs
    public void deleteTimeLogsForUser(User user) {
        LOGGER.info("Deleting time logs for user: {}", user.getUsername());
        List<TimeLog> timeLogs = timeLogRepository.findByTaskUser(user);
        timeLogRepository.deleteAll(timeLogs); // believed in the IDE and replace with the bulk... (!)
    }

    // trying streams to sum the duration
    public Duration getTotalWorkEffortByUserAndDataRange(User user, LocalDateTime start, LocalDateTime end) {
        LOGGER.debug("Getting total work effort for username {} id {} between {} and {}",user.getUsername(), user.getId(),start, end);
        return getTimeLogsByUserAndDateRange(user, start, end).stream()
                .filter(timeLog -> timeLog.getEndTime() != null) // only existed real TimeLogs
                .map(timeLog -> Duration.between(timeLog.getStartTime(), timeLog.getEndTime())) // trying to duration
                .reduce(Duration::plus) // sum durations
                .orElse(Duration.ZERO); // if no logs -> return ZERO
    }

    @Scheduled(cron = "0 59 23 * * ?")
    public void autoEndTasks() {
        LOGGER.info("Auto-ending ongoing tasks");
        List<TimeLog> ongoingTimeLog = timeLogRepository.findByEndTimeIsNull();
        ongoingTimeLog.forEach(timeLog -> {
            timeLog.setEndTime(LocalDateTime.now());
            timeLogRepository.save(timeLog);
        });
    }

    public void deleteOldTimeLogs(LocalDateTime cutoff) {
        LOGGER.info("Deleting old time logs before {}", cutoff);
        List<TimeLog> oldTimeLogs = timeLogRepository.findByStartTimeBefore(cutoff);
        timeLogRepository.deleteAll(oldTimeLogs);
    }

    public TimeLog getLastTimeLogForTask(Task task) {
        LOGGER.debug("Getting the last time log for task with id: {}", task.getId());
        return timeLogRepository.findFirstByTaskOrderByStartTimeDesc(task);
    }

    public Duration getTaskTimeElapsed(Task task) {
        LOGGER.debug("Getting time elapsed for task with id: {}", task.getId());
        TimeLog timeLog = timeLogRepository.findFirstByTaskAndEndTimeIsNullOrderByStartTimeDesc(task)
                .orElseThrow(() -> new OngoingTaskNotFoundException(task.getId()));
        return Duration.between(timeLog.getStartTime(), LocalDateTime.now());
    }
}
