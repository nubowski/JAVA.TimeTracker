package ru.nubowski.timeTracker.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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
    private final TimeLogRepository timeLogRepository;

    public TimeLogService(TimeLogRepository timeLogRepository) {
        this.timeLogRepository = timeLogRepository;
    }

    public List<TimeLog> getAllTimeLogs(){
        return timeLogRepository.findAll();
    }

    public TimeLog getTimeLog(Long id) {
        return timeLogRepository.findById(id).orElseThrow(() -> new RuntimeException("TimeLog not found"));
    }

    public TimeLog saveTimeLog(TimeLog timeLog) {
        return timeLogRepository.save(timeLog);
    }

    public void deleteTimeLog(Long id) {
        timeLogRepository.deleteById(id);
    }

    public TimeLog startTask (Task task) {
        TimeLog timeLog = new TimeLog();
        timeLog.setTask(task);
        timeLog.setStartTime(LocalDateTime.now());
        return timeLogRepository.save(timeLog);
    }

    public TimeLog stopTask(Task task) {
        TimeLog timeLog = timeLogRepository.findFirstByTaskAndEndTimeIsNullOrderByStartTimeDesc(task).orElseThrow(()
                -> new RuntimeException("No ongoing task found"));
        timeLog.setEndTime(LocalDateTime.now());
        timeLog.setEndedByUser(true);
        return timeLogRepository.save(timeLog);
    }

    public List<TimeLog> getTimeLogsByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end) {
        return timeLogRepository.findByUserAndDateRange(user, start, end);
    }

    //  delete time logs
    public void deleteTimeLogsForUser(User user) {
        List<TimeLog> timeLogs = timeLogRepository.findByTaskUser(user);
        timeLogRepository.deleteAll(timeLogs); // believed in the IDE and replace with the bulk... (!)
    }

    // trying streams to sum the duration
    public Duration getTotalWorkEffortByUserAndDataRange(User user, LocalDateTime start, LocalDateTime end) {
        return getTimeLogsByUserAndDateRange(user, start, end).stream()
                .filter(timeLog -> timeLog.getEndTime() != null) // only existed real TimeLogs
                .map(timeLog -> Duration.between(timeLog.getStartTime(), timeLog.getEndTime())) // trying to duration
                .reduce(Duration::plus) // sum durations
                .orElse(Duration.ZERO); // if no logs -> return ZERO
    }

    @Scheduled(cron = "0 59 23 * * ?")
    public void autoEndTasks() {
        List<TimeLog> ongoingTimeLog = timeLogRepository.findByEndTimeIsNull();
        ongoingTimeLog.forEach(timeLog -> {
            timeLog.setEndTime(LocalDateTime.now());
            timeLogRepository.save(timeLog);
        });
    }

    public void deleteOldTimeLogs(LocalDateTime cutoff) {
        List<TimeLog> oldTimeLogs = timeLogRepository.findByStartTimeBefore(cutoff);
        timeLogRepository.deleteAll(oldTimeLogs);
    }
}
