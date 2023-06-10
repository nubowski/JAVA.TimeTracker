package ru.nubowski.timeTracker.service;

import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.repository.UserRepository;

import java.util.List;

@Service // TODO do not fockup with @annotation as the previous time
public class UserService {
    private final UserRepository userRepository;
    private final TaskService taskService;
    private final TimeLogService timeLogService;

    public UserService(UserRepository userRepository, TaskService taskService, TimeLogService timeLogService) {
        this.userRepository = userRepository;
        this.taskService = taskService;
        this.timeLogService = timeLogService;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // findById is not a bad workaround, just let it be dummy
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUser(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    // for constantly delete `user` TODO: check @Autowired vs construction injection with `final` keyword
    public void deleteUserAndTasks(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        user.getTasks().forEach(task -> {
                timeLogService.deleteTimeLogsForUser(user);
                taskService.deleteTask(task.getId());
        });
        userRepository.delete(user);
    }
}
