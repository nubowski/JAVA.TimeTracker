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

    public List<TimeLog> getAllTimeLogsForTask(Task task) {
        LOGGER.debug("Getting all time logs of the task with id: {}", task.getId());
        return timeLogRepository.findAllByTask(task);
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

    public List<TimeLog> getTimeLogsByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end) {
        LOGGER.debug("Getting time logs for user {} between {} and {}", user.getUsername(), start, end);
        return timeLogRepository.findTST(user, start, end);
    }

    //  delete time logs
    public void deleteTimeLogsByTask(Task task) {
        LOGGER.info("Deleting time logs for task: {}", task.getName());
        List<TimeLog> timeLogs = timeLogRepository.findByTask(task);
        timeLogRepository.deleteAll(timeLogs); // believed in the IDE and replace with the bulk... (!)
    }

    // trying streams to sum the duration
    public Duration getTotalWorkEffortByUserAndDataRange(User user, LocalDateTime start, LocalDateTime end) {
        LOGGER.info("Getting total work effort for username {} id {} between {} and {}", user.getUsername(), user.getId(), start, end);
        return Duration.ofSeconds(timeLogRepository.getTotalWorkEffortInSeconds(user.getId(), start, end));
    }

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

    @Scheduled(cron = "0 59 23 * * ?")
    public void autoEndTasks() {
        LOGGER.info("Auto-ending ongoing tasks");
        List<TimeLog> ongoingTimeLog = timeLogRepository.findByEndTimeIsNullAndTaskStateEquals(TaskState.ONGOING); // PAUSE
        ongoingTimeLog.forEach(timeLog -> {
            timeLog.setEndTime(LocalDateTime.now()); // TODO: LocalDateTime out of loop --^ condition ONGOING on each + bulk
            timeLog.setTaskState(TaskState.AUTO_STOPPED);
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
        return timeLogRepository.findFirstByTaskOrderByStartTimeDesc(task)
                .orElseThrow(() -> new TimeLogNotFoundException(task.getId()));
    }

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
}
