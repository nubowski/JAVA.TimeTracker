package ru.nubowski.timeTracker.service;

import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.model.TimeLog;
import ru.nubowski.timeTracker.repository.TimeLogRepository;

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
}
