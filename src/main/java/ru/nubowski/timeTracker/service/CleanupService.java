package ru.nubowski.timeTracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.config.CleanupProperties;
import ru.nubowski.timeTracker.exception.CleanupFailedException;

import java.time.LocalDateTime;
@Service
public class CleanupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupService.class);
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
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(cleanupProperties.getRetentionPeriod());
            LOGGER.info("Cleanup started at {}", cutoff);
            taskService.deleteOldTasks(cutoff);
            timeLogService.deleteOldTimeLogs(cutoff);
            userService.deleteOldUsers(cutoff);
            LOGGER.info("Cleanup completed successfully");
        } catch (CleanupFailedException e) { // TODO: some research of how it should be in LIVE
            LOGGER.error("Cleanup failed due to a specific problem", e);
            throw e;
        }
    }
}
