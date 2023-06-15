package ru.nubowski.timeTracker.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nubowski.timeTracker.dto.UserCreateRequest;
import ru.nubowski.timeTracker.dto.UserUpdateRequest;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        LOGGER.info("Received request to get all users");
        List<User> users = userService.getAllUsers();
        LOGGER.info("Responding with {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        LOGGER.info("Received request to get user with username: {}", username);
        User user = userService.getUser(username);
        LOGGER.info("Responding with user {}", user);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserCreateRequest request) {
        LOGGER.info("Received request to create user: {}", request.getUsername());

        // check if already exists
        Optional<User> existingUser = userService.getUserByUsername(request.getUsername());
        if (existingUser.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // if user doesn't exist
        User userToCreate = userService.mapToUser(request);
        User createdUser = userService.saveUser(userToCreate);
        LOGGER.info("Created user: {}", createdUser.getUsername());
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{username}") // TODO: all is good until username is unique entity, if not - auth/auth/another approach
    public ResponseEntity<User> updateUser(@PathVariable String username, @Valid @RequestBody UserUpdateRequest request) {
        LOGGER.info("Received request to update user with username: {} with data {}", username, request);

        Optional<User> existingUser = userService.getUserByUsername(username);
        if (existingUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        User userToUpdate = userService.mapToUpgradeUser(request, username);
        User updatedUser = userService.saveUser(userToUpdate);
        LOGGER.info("Updated user with username: {}", updatedUser.getUsername());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

        /*LOGGER.info("Received request to delete user with username: {}", username);
        userService.deleteUser(username);
        LOGGER.info("Deleted user with username: {}", username);
        return ResponseEntity.noContent().build();*/
    }

    @DeleteMapping("/{username}/delete")
    public ResponseEntity<Void> deleteUserAndTasks(@PathVariable String username) {
        LOGGER.info("Received request to delete user with username: {} and all the their tasks", username);
        userService.deleteTimeLogsAndTasks(username);
        LOGGER.info("Deleted user with username: {} and all their tasks", username);
        userService.deleteUser(username); // doubt about double controller reset/delete, should be 1 with keys
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{username}/reset")
    public ResponseEntity<User> resetTimeLogsAndTasks(@PathVariable String username) {
        LOGGER.info("Received request to delete user with username: {} and all the their tasks", username);
        userService.deleteTimeLogsAndTasks(username);
        LOGGER.info("Deleted user with username: {} and all their tasks", username);
        User user = userService.getUser(username);
        return ResponseEntity.ok(user);
    }

}
