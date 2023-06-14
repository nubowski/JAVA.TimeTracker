package ru.nubowski.timeTracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nubowski.timeTracker.exception.UserNotFoundException;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.repository.TaskRepository;
import ru.nubowski.timeTracker.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service // TODO do not fockup with @annotation as the previous time
public class UserService {
    private static final  Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final TaskService taskService;
    private final TimeLogService timeLogService;
    private final TaskRepository taskRepository;

    public UserService(UserRepository userRepository, TaskService taskService, TimeLogService timeLogService, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskService = taskService;
        this.timeLogService = timeLogService;
        this.taskRepository = taskRepository;
    }

    public List<User> getAllUsers() {
        LOGGER.debug("Getting all users");
        return userRepository.findAll();
    }

    // findById is not a bad workaround, just let it be dummy
    public User getUserById(Long id) {
        LOGGER.debug("Getting user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User getUser(String username) {
        LOGGER.debug("Getting user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    public User saveUser(User user) {
        if (user == null || user.getUsername() == null) {
            throw new IllegalArgumentException("Username can't be null");
        }
        LOGGER.info("Saving user: {}", user.getUsername());
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            // if user exists, update fields
            User userToUpdate = existingUser.get();
            BeanUtils.copyProperties(user, userToUpdate, getNullPropertyNames(user));
            return userRepository.save(userToUpdate);
        } else {
            // if user doesn't exist - create
            if (user.getId() == null) {
                user.setCreatedAt(LocalDateTime.now()); // first time created add timestamp
            }
            return userRepository.save(user);
        }
    }

    // utility method null properties in a bean
    private static String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for(java.beans.PropertyDescriptor pd : pds) {
            // check if value of this property is null then add it to the collection
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public void deleteUser(String username) {
        LOGGER.info("Deleting user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        userRepository.delete(user);
    }
    @Transactional
    public void deleteUserAndTasks(String username) {
        LOGGER.info("Deleting user and associated tasks: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        List<Task> tasks = taskRepository.findByUserId(user.getId());

        tasks.forEach(task -> {
            LOGGER.info("Deleting timeLogs for task: {}", task.getId());
            timeLogService.deleteTimeLogsByTask(task);
            LOGGER.info("TimeLogs deleted for task: {}", task.getId());
            LOGGER.info("Deleting task: {}", task.getId());
            taskService.deleteTask(task.getId());
            LOGGER.info("Task deleted: {}", task.getId());
        });

        //        LOGGER.info("Deleting user: {}", username);
        //        userRepository.delete(user);
        //        LOGGER.info("User deleted: {}", username);
    }

    public void resetTimeLogsAndTasks(String username) {
        LOGGER.info("Clearing all tracker history for user {}", username);
    }

    public void deleteOldUsers(LocalDateTime cutoff) {
        LOGGER.info("Deleting users created before {}", cutoff);
        List<User> oldUsers = userRepository.findByCreatedAtBefore(cutoff);
        userRepository.deleteAll(oldUsers);
    }
}
