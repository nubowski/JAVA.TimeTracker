package ru.nubowski.timeTracker.service;

import org.springframework.scheduling.annotation.Scheduled;
import ru.nubowski.timeTracker.config.CleanupProperties;

import java.time.LocalDateTime;

public class CleanupService {
    private final TaskService taskService;
    private final UserService userService;
    private final TimeLogService timeLogService;
    private final CleanupProperties cleanupProperties;

    public CleanupService(TaskService taskService, UserService userService, TimeLogService timeLogService, CleanupProperties cleanupProperties) {
        this.taskService = taskService;
        this.userService = userService;
        this.timeLogService = timeLogService;
        this.cleanupProperties = cleanupProperties;
    }

    @Scheduled(cron = "#{cleanupProperties.getCronExpression()}") // double check cron expression every time
    public void cleanup() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(cleanupProperties.getRetentionPeriod());
        taskService.deleteOldTasks(cutoff);
        timeLogService.deleteOldTimeLogs(cutoff);
        userService.deleteOldUsers(cutoff);
    }
}
