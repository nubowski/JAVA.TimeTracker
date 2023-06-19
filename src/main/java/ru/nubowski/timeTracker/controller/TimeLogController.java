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

/**
 * Controller for handling time log related endpoints.
 */
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

    /**
     * Returns all time logs.
     *
     * @return a list of all time logs.
     */
    @GetMapping
    public ResponseEntity<List<TimeLog>> getAllTimeLogs() {
        LOGGER.info("Received request to get all time logs");
        List<TimeLog> timeLogs = timeLogService.getAllTimeLogs();
        LOGGER.info("Responding with {} time logs", timeLogs.size());
        return ResponseEntity.ok(timeLogs);
    }

    /**
     * Returns the time logs associated with the given task id.
     *
     * @param taskId the id of the task whose time logs are to be retrieved.
     * @return a list of time logs associated with the given task id.
     */
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

    /**
     * Returns the time log with the given id.
     *
     * @param id the id of the time log to retrieve.
     * @return the time log with the given id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TimeLogByIdResponse> getTimeLog(@PathVariable Long id) {
        LOGGER.info("Received request to get time log with id {}", id);
        TimeLog timeLog = timeLogService.getTimeLog(id);
        TimeLogByIdResponse timeLogResponse = new TimeLogByIdResponse(timeLog);
        LOGGER.info("Responding with time log with id {}", id);
        return ResponseEntity.ok(timeLogResponse);
    }

    /**
     * Creates a new time log.
     *
     * @param timeLog the details of the time log to be created.
     * @return the created time log.
     */
    @PostMapping
    public ResponseEntity<TimeLog> createTimeLog(@RequestBody TimeLog timeLog) {
        LOGGER.info("Received request to create new time log");
        TimeLog savedTimeLog = timeLogService.saveTimeLog(timeLog);
        LOGGER.info("Created time log with id {}", savedTimeLog.getId());
        return new ResponseEntity<>(savedTimeLog, HttpStatus.CREATED);
    }

    /**
     * Deletes the time log with the given id.
     *
     * @param id the id of the time log to be deleted.
     * @return a response entity with HTTP status 204.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeLog(@PathVariable Long id) {
        LOGGER.info("Received request to delete time log with id {}", id);
        timeLogService.deleteTimeLog(id);
        LOGGER.info("Deleted time log with id {}", id);
        return ResponseEntity.noContent().build();
    }
}
