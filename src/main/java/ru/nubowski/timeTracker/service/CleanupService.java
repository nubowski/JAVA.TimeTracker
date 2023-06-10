package ru.nubowski.timeTracker.service;

import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

public class CleanupService {
    private final TaskService taskService;
    private final UserService userService;
    private final TimeLogService timeLogService;

    public CleanupService(TaskService taskService, UserService userService, TimeLogService timeLogService) {
        this.taskService = taskService;
        this.userService = userService;
        this.timeLogService = timeLogService;
    }

    @Scheduled(cron = "0 0 0 * * ?") // double check cron expression every time
    public void cleanup() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        taskService.deleteOldTasks(cutoff);
        timeLogService.deleteOldTimeLogs(cutoff);
        userService.deleteOldUsers(cutoff);
    }
}
