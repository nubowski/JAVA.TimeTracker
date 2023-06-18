package ru.nubowski.timeTracker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nubowski.timeTracker.dto.response.TimeLogByIdResponse;
import ru.nubowski.timeTracker.dto.response.TimeLogsByTaskResponse;
import ru.nubowski.timeTracker.mapper.TimeLogMapper;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.TimeLog;
import ru.nubowski.timeTracker.service.impl.TaskService;
import ru.nubowski.timeTracker.service.impl.TimeLogService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/time_logs")
public class TimeLogController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeLogController.class);
    private final TimeLogMapper timeLogMapper;
    private final TimeLogService timeLogService;
    private final TaskService taskService;

    public TimeLogController(TimeLogMapper timeLogMapper, TimeLogService timeLogService, TaskService taskService) {
        this.timeLogMapper = timeLogMapper;
        this.timeLogService = timeLogService;
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TimeLog>> getAllTimeLogs() {
        LOGGER.info("Received request to get all time logs");
        List<TimeLog> timeLogs = timeLogService.getAllTimeLogs();
        LOGGER.info("Responding with {} time logs", timeLogs.size());
        return ResponseEntity.ok(timeLogs);
    }

    // TODO need to convent of where is where (task->time-logs OR time-logs->task)
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TimeLogsByTaskResponse>> getTimeLogsByTask(@PathVariable Long taskId) {
        LOGGER.info("Received request to get time logs for task with id {}", taskId);
        Task task = taskService.getTask(taskId);
        List<TimeLog> timeLogs = timeLogService.getAllTimeLogsForTask(task);
        List<TimeLogsByTaskResponse> responses = timeLogs.stream()
                .map(timeLogMapper::mapTimeLogToResponse)
                .collect(Collectors.toList());
        LOGGER.info("Responding with {} time logs for task with id {}", timeLogs.size(), taskId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeLogByIdResponse> getTimeLog(@PathVariable Long id) {
        LOGGER.info("Received request to get time log with id {}", id);
        TimeLog timeLog = timeLogService.getTimeLog(id);
        TimeLogByIdResponse timeLogResponse = new TimeLogByIdResponse(timeLog);
        LOGGER.info("Responding with time log with id {}", id);
        return ResponseEntity.ok(timeLogResponse);
    }

    @PostMapping
    public ResponseEntity<TimeLog> createTimeLog(@RequestBody TimeLog timeLog) {
        LOGGER.info("Received request to create new time log");
        TimeLog savedTimeLog = timeLogService.saveTimeLog(timeLog);
        LOGGER.info("Created time log with id {}", savedTimeLog.getId());
        return new ResponseEntity<>(savedTimeLog, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeLog(@PathVariable Long id) {
        LOGGER.info("Received request to delete time log with id {}", id);
        timeLogService.deleteTimeLog(id);
        LOGGER.info("Deleted time log with id {}", id);
        return ResponseEntity.noContent().build();
    }
}
