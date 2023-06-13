package ru.nubowski.timeTracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.exception.OngoingTaskNotFoundException;
import ru.nubowski.timeTracker.exception.TaskNotFoundException;
import ru.nubowski.timeTracker.exception.TimeLogNotFoundException;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.TaskState;
import ru.nubowski.timeTracker.model.TimeLog;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.repository.TimeLogRepository;
import ru.nubowski.timeTracker.util.ClockProvider;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimeLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeLogService.class);
    private final TimeLogRepository timeLogRepository;
    private final ClockProvider clockProvider;

    public TimeLogService(TimeLogRepository timeLogRepository, ClockProvider clockProvider) {
        this.timeLogRepository = timeLogRepository;
        this.clockProvider = clockProvider;
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

    public TimeLog startTask(Task task) {
        LOGGER.info("Starting a task: {}", task.getId());
        TimeLog timeLog = new TimeLog();
        timeLog.setTask(task);
        timeLog.setStartTime(clockProvider.now());
        timeLog.setTaskState(TaskState.ONGOING);
        return timeLogRepository.save(timeLog);
    }

    public TimeLog stopTask(Task task) {
        LOGGER.info("Stopping task: {}", task.getId());
        TimeLog timeLog = timeLogRepository.findFirstByTaskAndEndTimeIsNullOrderByStartTimeDesc(task). // perfect naming ^-^
                orElseThrow(() -> new OngoingTaskNotFoundException(task.getId()));
        timeLog.setEndTime(clockProvider.now());
        timeLog.setTaskState(TaskState.USER_STOPPED);
        return timeLogRepository.save(timeLog);
    }

    public List<TimeLog> getTimeLogsByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end) {
        LOGGER.debug("Getting time logs for user {} between {} and {}", user.getUsername(), start, end); // we still have userID
        // logs where startTime is after 'start' and (endTime is before 'end' or endTime is null)
        List<TimeLog> completedTimeLogs = timeLogRepository.findTimeLogByTaskUserAndStartTimeAfterAndEndTimeBefore(user, start, end);
        List<TimeLog> ongoingTimeLogs = timeLogRepository.findTimeLogByTaskUserAndStartTimeAfterAndEndTimeIsNull(user, start);
        // both lists
        completedTimeLogs.addAll(ongoingTimeLogs);
        return completedTimeLogs;
    }

    //  delete time logs
    public void deleteTimeLogsByTask(Task task) {
        LOGGER.info("Deleting time logs for task: {}", task.getName());
        List<TimeLog> timeLogs = timeLogRepository.findByTask(task);
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

    public TimeLog pauseTask(Task task) {
        LOGGER.info("Pausing task: {}", task.getId());
        TimeLog timeLog = timeLogRepository.findFirstByTaskAndEndTimeIsNullOrderByStartTimeDesc(task).
                orElseThrow(() -> new OngoingTaskNotFoundException(task.getId()));
        timeLog.setEndTime(clockProvider.now());
        timeLog.setTaskState(TaskState.PAUSED);
        return timeLogRepository.save(timeLog);
    }

    public TimeLog resumeTask(Task task) {
        LOGGER.info("Resuming task: {}", task.getId());
//      Some logic here, if we need that. Semantically better view to save `resume` as a separate method for now.
        return startTask(task);
    }
}
