package ru.nubowski.timeTracker.service;

import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.TimeLog;
import ru.nubowski.timeTracker.repository.TimeLogRepository;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
}
