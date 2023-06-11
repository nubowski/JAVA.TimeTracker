package ru.nubowski.timeTracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.exception.UserNotFoundException;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service // TODO do not fockup with @annotation as the previous time
public class UserService {
    private static final  Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final TaskService taskService;
    private final TimeLogService timeLogService;

    public UserService(UserRepository userRepository, TaskService taskService, TimeLogService timeLogService) {
        this.userRepository = userRepository;
        this.taskService = taskService;
        this.timeLogService = timeLogService;
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
        LOGGER.info("Saving user: {}", user.getUsername());
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            // if exists udp fields
            User userToUpdate = existingUser.get();
            userToUpdate.setDisplayName(user.getDisplayName());
            userToUpdate.setEmail(user.getEmail());
            userToUpdate.setTasks(user.getTasks());
            // .. additional fields for update
            return userRepository.save(userToUpdate);
        } else {
            // if not exists - create
            if (user.getId() == null) {
                user.setCreatedAt(LocalDateTime.now()); // firstly created add timestamp TODO: add to an update
            }
            return userRepository.save(user);
        }
    }

    public void deleteUser(String username) {
        LOGGER.info("Deleting user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        userRepository.delete(user);
    }

    // for constantly delete `user`
    // TODO: check @Autowired vs construction injection with `final` keyword
    public void deleteUserAndTasks(String username) {
        LOGGER.info("Deleting user and associated tasks: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        user.getTasks().forEach(task -> {
                timeLogService.deleteTimeLogsForUser(user);
                taskService.deleteTask(task.getId());
        });
        userRepository.delete(user);
    }

    public void deleteOldUsers(LocalDateTime cutoff) {
        LOGGER.info("Deleting users created before {}", cutoff);
        List<User> oldUsers = userRepository.findByCreatedAtBefore(cutoff);
        userRepository.deleteAll(oldUsers);
    }
}
