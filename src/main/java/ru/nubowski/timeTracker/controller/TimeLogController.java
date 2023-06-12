package ru.nubowski.timeTracker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.TimeLog;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.service.TaskService;
import ru.nubowski.timeTracker.service.TimeLogService;
import ru.nubowski.timeTracker.service.UserService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/time_logs")
public class TimeLogController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeLogController.class);
    private final TimeLogService timeLogService;
    private final TaskService taskService;
    private final UserService userService;

    public TimeLogController(TimeLogService timeLogService, TaskService taskService, UserService userService) {
        this.timeLogService = timeLogService;
        this.taskService = taskService;
        this.userService = userService;
    }

    @PostMapping("/start/{taskId}")
    public ResponseEntity<TimeLog> startTask(@PathVariable Long taskId) {
        LOGGER.info("Received request to start task with id {}", taskId);
        try {
            Task task = taskService.getTask(taskId);
            TimeLog timeLog = timeLogService.startTask(task);
            LOGGER.info("Task with id {} has been started", taskId);  // dont think its necessary coz of custom ex, but just in case
            return new ResponseEntity<>(timeLog, HttpStatus.CREATED);
        } catch (Exception e) {
            LOGGER.error("Error occurred while starting task with id {}", taskId, e);
            throw e;
        }
    }

    @PostMapping("/stop/{taskId}")
    public ResponseEntity<TimeLog> stopTask(@PathVariable Long taskId) {
        LOGGER.info("Received request to stop task with id {}", taskId);
        Task task = taskService.getTask(taskId);
        TimeLog timeLog = timeLogService.stopTask(task);
        LOGGER.info("Task with id {} has been stopped", taskId);
        return ResponseEntity.ok(timeLog);
    }

    @GetMapping
    public ResponseEntity<List<TimeLog>> getAllTimeLogs() {
        LOGGER.info("Received request to get all time logs");
        List<TimeLog> timeLogs = timeLogService.getAllTimeLogs();
        LOGGER.info("Responding with {} time logs", timeLogs.size());
        return ResponseEntity.ok(timeLogs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeLog> getTimeLog(@PathVariable Long id) {
        LOGGER.info("Received request to get time log with id {}", id);
        TimeLog timeLog = timeLogService.getTimeLog(id);
        LOGGER.info("Responding with time log with id {}", id);
        return ResponseEntity.ok(timeLog);
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

    @GetMapping("/user/{username}/date_range")
    public ResponseEntity<List<String>> getTimeLogsByUserAndDateRange(
            @PathVariable String username,
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end,
            @RequestParam(value = "sort", required = false, defaultValue = "duration")
            String sort) {

        LOGGER.info("Fetching time logs for user {} in date range from {} to {}", username, start, end);
        User user = userService.getUser(username);
        List<TimeLog> timeLogs = timeLogService.getTimeLogsByUserAndDateRange(user, start, end);

        // Sorting by start time
        if (sort.equals("start_time")) {
            timeLogs.sort(Comparator.comparing(TimeLog::getStartTime));
        }

        Map<Task, Long> durationsPerTask = timeLogs.stream()
                .collect(Collectors.groupingBy(
                        TimeLog::getTask,
                        Collectors.summingLong(log -> Duration.between(log.getStartTime(), log.getEndTime()).toMillis())
                ));

        Stream<Map.Entry<Task, Long>> stream = durationsPerTask.entrySet().stream();

        // by duration if requested
        if (sort.equals("duration")) {
            stream = stream.sorted(Map.Entry.<Task, Long>comparingByValue().reversed());
        }

        List<String> formattedTimeLogs = stream
                .map(entry -> {
                    Task task = entry.getKey();
                    Duration duration = Duration.ofMillis(entry.getValue());
                    return String.format("%s - %02d:%02d",
                            task.getName(),
                            duration.toHours(),
                            duration.toMinutesPart());
                })
                .collect(Collectors.toList());

        LOGGER.info("Fetched {} time logs for user {} in date range from {} to {}", formattedTimeLogs.size(), username, start,end);
        return ResponseEntity.ok(formattedTimeLogs);
    }


    // only completed TimeLogs, TODO: add a method for check and add ongoing tasks too
    @GetMapping("/user/{username}/work_effort")
    public ResponseEntity<Duration> getTotalWorkEffortByUserAndDateRange(
            @PathVariable String username,
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end) {

        LOGGER.info("Calculating total work effort for user {} in date range from {} to {}", username, start, end);
        User user = userService.getUser(username);
        Duration totalWorkEffort = timeLogService.getTotalWorkEffortByUserAndDataRange(user, start, end);
        LOGGER.info("Total work effort for user {} id date range from {} to {} is {}", username, start, end, totalWorkEffort);
        return ResponseEntity.ok(totalWorkEffort);
    }

    @GetMapping("/task/{taskId}/time_elapsed")
    public ResponseEntity<Duration> getTaskTimeElapsed(@PathVariable Long taskId) {
        LOGGER.info("Received request to get time elapsed for task with id {}", taskId);
        Task task = taskService.getTask(taskId);
        Duration timeElapsed = timeLogService.getTaskTimeElapsed(task);
        LOGGER.info("Time elapsed for task with id {} is {}", taskId, timeElapsed);
        return ResponseEntity.ok(timeElapsed);
    }

    @PostMapping("/pause/{taskId}")
    public ResponseEntity<TimeLog> pauseTask(@PathVariable Long taskId) {
        LOGGER.info("Received request to pause task with id {}", taskId);
        Task task = taskService.getTask(taskId);
        TimeLog timeLog = timeLogService.pauseTask(task);
        LOGGER.info("Task with id {} has been paused", taskId);
        return ResponseEntity.ok(timeLog);
    }

    @PostMapping("/resume/{taskId}")
    public ResponseEntity<TimeLog> resumeTask(@PathVariable Long taskId) {
        LOGGER.info("Received request to resume task with id {}", taskId);
        Task task = taskService.getTask(taskId);
        TimeLog timeLog = timeLogService.resumeTask(task);
        LOGGER.info("Task with id {} has been resumed", taskId);
        return ResponseEntity.ok(timeLog);
    }
}
