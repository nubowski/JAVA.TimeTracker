package ru.nubowski.timeTracker.controller;

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
import java.util.List;

@RestController
@RequestMapping("/time_logs")
public class TimeLogController {
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
        Task task = taskService.getTask(taskId);
        TimeLog timeLog = timeLogService.startTask(task);
        return new ResponseEntity<>(timeLog, HttpStatus.CREATED);
    }

    @PostMapping("/stop/{taskId}")
    public ResponseEntity<TimeLog> stopTask(@PathVariable Long taskId) {
        Task task = taskService.getTask(taskId);
        TimeLog timeLog = timeLogService.stopTask(task);
        return ResponseEntity.ok(timeLog);
    }

    @GetMapping
    public ResponseEntity<List<TimeLog>> getAllTimeLogs() {
        return ResponseEntity.ok(timeLogService.getAllTimeLogs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeLog> getTimeLog(@PathVariable Long id) {
        return ResponseEntity.ok(timeLogService.getTimeLog(id));
    }

    @PostMapping
    public ResponseEntity<TimeLog> createTimeLog(@RequestBody TimeLog timeLog) {
        return new ResponseEntity<>(timeLogService.saveTimeLog(timeLog), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeLog(@PathVariable Long id) {
        timeLogService.deleteTimeLog(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{username}/date_range") // change data format later on TODO: refactor this ---V
    public ResponseEntity<List<TimeLog>> getTimeLogsByUserAndDateRange(@PathVariable String username,
                                                                       @RequestParam("start")
                                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                       LocalDateTime start,
                                                                       @RequestParam("end")
                                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                       LocalDateTime end) {
        User user = userService.getUser(username);
        return ResponseEntity.ok(timeLogService.getTimeLogsByUserAndDateRange(user, start, end));
    }

    // only completed TimeLogs, TODO: add a method for check and add ongoing tasks too
    @GetMapping("/user/{username}/work_effort")
    public ResponseEntity<Duration> getTotalWorkEffortByUserAndDateRange(@PathVariable String username,
                                                                         @RequestParam("start")
                                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                         LocalDateTime start,
                                                                         @RequestParam("end")
                                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                         LocalDateTime end) {
        User user = userService.getUser(username);
        return ResponseEntity.ok(timeLogService.getTotalWorkEffortByUserAndDataRange(user, start, end));
    }
}
