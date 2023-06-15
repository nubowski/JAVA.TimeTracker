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
import ru.nubowski.timeTracker.service.impl.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProcessService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupService.class);
    private final TaskService taskService;
    private final UserService userService;
    private final TimeLogService timeLogService;

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    public ProcessService(TaskService taskService, UserService userService, TimeLogService timeLogService, UserRepository userRepository, TaskRepository taskRepository) {
        this.taskService = taskService;
        this.userService = userService;
        this.timeLogService = timeLogService;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

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

    public void deleteOldUsers(LocalDateTime cutoff) {
        LOGGER.info("Deleting users created before {}", cutoff);
        List<User> oldUsers = userRepository.findByCreatedAtBefore(cutoff);
        oldUsers.stream()
                .map(User::getUsername)
                .forEach(this::deleteTimeLogsAndTasks);
        userRepository.deleteAll(oldUsers);
    }
}
