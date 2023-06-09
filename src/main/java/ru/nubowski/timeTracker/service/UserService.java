package ru.nubowski.timeTracker.service;

import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.repository.UserRepository;

import java.util.List;

@Service // TODO do not fockup with @annotation as the previous time
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
