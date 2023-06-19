package ru.nubowski.timeTracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.config.CleanupProperties;
import ru.nubowski.timeTracker.exception.CleanupFailedException;
import ru.nubowski.timeTracker.service.impl.TaskService;
import ru.nubowski.timeTracker.service.impl.TimeLogService;

import java.time.LocalDateTime;
/**
 * Service for performing scheduled cleanup operations.
 */
@Service
public class CleanupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupService.class);
    private final TaskService taskService;
    private final TimeLogService timeLogService;
    private final ProcessService processService;
    private final CleanupProperties cleanupProperties;

    /**
     * Constructor for CleanupService.
     *
     * @param taskService         the service for handling tasks
     * @param timeLogService      the service for handling time logs
     * @param processService      the service for handling orchestral complex things with several services
     * @param cleanupProperties   the properties used for configuring cleanup
     */
    public CleanupService(TaskService taskService, TimeLogService timeLogService, ProcessService processService, CleanupProperties cleanupProperties) {
        this.taskService = taskService;
        this.timeLogService = timeLogService;
        this.processService = processService;
        this.cleanupProperties = cleanupProperties;
    }

    /**
     * Scheduled method for cleaning up old tasks, time logs, and users.
     * The cleanup frequency is determined by a cron expression defined in cleanupProperties.
     *
     * @throws CleanupFailedException if cleanup fails due to a specific problem
     */
    @Scheduled(cron = "#{cleanupProperties.getCronExpression()}") // double check cron expression every time
    public void cleanup() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(cleanupProperties.getRetentionPeriod());
            LOGGER.info("Cleanup started at {}", cutoff);
            taskService.deleteOldTasks(cutoff);
            timeLogService.deleteOldTimeLogs(cutoff);
            processService.deleteOldUsers(cutoff);
            LOGGER.info("Cleanup completed successfully");
        } catch (CleanupFailedException e) { // TODO: some research of how it should be in LIVE
            LOGGER.error("Cleanup failed due to a specific problem", e);
            throw e;
        }
    }
}
