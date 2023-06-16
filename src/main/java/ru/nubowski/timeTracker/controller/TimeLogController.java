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
import ru.nubowski.timeTracker.service.impl.TaskService;
import ru.nubowski.timeTracker.service.impl.TimeLogService;
import ru.nubowski.timeTracker.service.impl.UserService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @GetMapping
    public ResponseEntity<List<TimeLog>> getAllTimeLogs() {
        LOGGER.info("Received request to get all time logs");
        List<TimeLog> timeLogs = timeLogService.getAllTimeLogs();
        LOGGER.info("Responding with {} time logs", timeLogs.size());
        return ResponseEntity.ok(timeLogs);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TimeLog>> getTimeLogsByTask(@PathVariable Long taskId) {
        LOGGER.info("Received request to get time logs for task with id {}", taskId);
        Task task = taskService.getTask(taskId);
        List<TimeLog> timeLogs = timeLogService.getAllTimeLogsForTask(task);
        LOGGER.info("Responding with {} time logs for task with id {}", timeLogs.size(), taskId);
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

    @GetMapping("/user/{username}/date_range") // TODO clean this up and make it lean. Kick all the logic out
    public ResponseEntity<List<String>> getTimeLogsByUserAndDateRange(
            @PathVariable String username,
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end,
            @RequestParam(value = "sort", required = false, defaultValue = "duration")
            String sort,
            @RequestParam(value = "output", required = false, defaultValue = "duration")
            String output) {

        LOGGER.info("Fetching time logs for user {} in date range from {} to {}", username, start, end);
        User user = userService.getUser(username);
        List<TimeLog> timeLogs = timeLogService.getTimeLogsByUserAndDateRange(user, start, end);

        // by start time if requested
        if (sort.equals("start_time")) {
            timeLogs.sort(Comparator.comparing(TimeLog::getStartTime));
        }

        Map<Task, Long> durationsPerTask = timeLogs.stream()
                .collect(Collectors.groupingBy(
                        TimeLog::getTask,
                        Collectors.summingLong(log -> Duration.between(log.getStartTime(), log.getEndTime() != null ? log.getEndTime() : end.isBefore(LocalDateTime.now()) ? end : LocalDateTime.now()).toMillis())
                ));

        Stream<Map.Entry<Task, Long>> stream = durationsPerTask.entrySet().stream();

        // by duration if requested
        if (sort.equals("duration")) {
            stream = stream.sorted(Map.Entry.<Task, Long>comparingByValue().reversed());
        }

        List<String> formattedTimeLogs;

        // choose output format
        if (output.equals("duration")) {
            formattedTimeLogs = stream
                    .map(entry -> {
                        Task task = entry.getKey();
                        Duration duration = Duration.ofMillis(entry.getValue());
                        return String.format("%s - %02d:%02d",
                                task.getName(),
                                duration.toHours(),
                                duration.toMinutesPart());
                    })
                    .collect(Collectors.toList());
        } else {  // "interval"
            formattedTimeLogs = timeLogs.stream()
                    .map(log -> String.format("%s - %s | %s",
                            log.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            log.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            log.getTask().getName()))
                    .collect(Collectors.toList());
        }
        LOGGER.info("Fetched {} time logs for user {} in date range from {} to {}", formattedTimeLogs.size(), username, start, end);
        return ResponseEntity.ok(formattedTimeLogs);
    }


    // only completed TimeLogs, TODO: add a method for check and add ongoing tasks too
    @GetMapping("/user/{username}/work_effort")
    public ResponseEntity<String> getTotalWorkEffortByUserAndDateRange(
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
        String formattedTotalWorkEffort = String.format("%02d:%02d", totalWorkEffort.toHours(), totalWorkEffort.toMinutesPart());
        LOGGER.info("Total work effort for user {} in date range from {} to {} is {}", username, start, end, formattedTotalWorkEffort);
        return ResponseEntity.ok(formattedTotalWorkEffort);
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
        TimeLog timeLog = taskService.pauseTask(task);
        LOGGER.info("Task with id {} has been paused", taskId);
        return ResponseEntity.ok(timeLog);
    }

    @PostMapping("/resume/{taskId}")
    public ResponseEntity<TimeLog> resumeTask(@PathVariable Long taskId) {
        LOGGER.info("Received request to resume task with id {}", taskId);
        Task task = taskService.getTask(taskId);
        TimeLog timeLog = taskService.resumeTask(task);
        LOGGER.info("Task with id {} has been resumed", taskId);
        return ResponseEntity.ok(timeLog);
    }
}
