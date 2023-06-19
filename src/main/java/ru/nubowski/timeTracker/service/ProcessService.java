package ru.nubowski.timeTracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nubowski.timeTracker.exception.UserNotFoundException;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.repository.TaskRepository;
import ru.nubowski.timeTracker.repository.UserRepository;
import ru.nubowski.timeTracker.service.impl.TaskService;
import ru.nubowski.timeTracker.service.impl.TimeLogService;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Service for managing processes related to all main services. UserService, TaskService, TimeLogService
 */
@Service
public class ProcessService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupService.class);
    private final TaskService taskService;
    private final TimeLogService timeLogService;

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    /**
     * Constructor for ProcessService.
     *
     * @param taskService       the service for handling tasks
     * @param timeLogService    the service for handling time logs
     * @param userRepository    the repository for handling users
     * @param taskRepository    the repository for handling tasks
     */
    public ProcessService(TaskService taskService, TimeLogService timeLogService, UserRepository userRepository, TaskRepository taskRepository) {
        this.taskService = taskService;
        this.timeLogService = timeLogService;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Deletes a user and all tasks associated with that user.
     *
     * @param username  the name of the user to be deleted
     * @throws UserNotFoundException if the specified user is not found
     */
    @Transactional
    public void deleteTimeLogsAndTasks(String username) {
        LOGGER.info("Deleting user and associated tasks: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        List<Task> tasks = taskRepository.findByUserId(user.getId());
        tasks.forEach(task -> {
            LOGGER.debug("Deleting timeLogs for task: {}", task.getId());
            timeLogService.deleteTimeLogsByTask(task);
            LOGGER.debug("TimeLogs deleted for task: {}", task.getId());
            LOGGER.debug("Deleting task: {}", task.getId());
            taskService.deleteTask(task.getId());
            LOGGER.debug("Task deleted: {}", task.getId());
        });
    }

    /**
     * Deletes all users who were created before a specific date and time. Part of the cleanup  service.
     *
     * @param cutoff  the date and time to use as a cutoff for deletion
     */
    public void deleteOldUsers(LocalDateTime cutoff) {
        LOGGER.info("Deleting users created before {}", cutoff);
        List<User> oldUsers = userRepository.findByCreatedAtBefore(cutoff);
        oldUsers.stream()
                .map(User::getUsername)
                .forEach(this::deleteTimeLogsAndTasks);
        userRepository.deleteAll(oldUsers);
    }
}
