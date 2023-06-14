// TODO check 2 saved articles of controller usage with Spring approach

package ru.nubowski.timeTracker.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.service.UserService;

import java.util.List;

@RestController
@Api(description = "User Controller")
@RequestMapping("/users")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @ApiOperation(value = "This endpoint returns all users")
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
    public ResponseEntity<User> createUser(@RequestBody User user) {
        LOGGER.info("Received request to create user: {}", user);
        User createdUser = userService.saveUser(user);
        LOGGER.info("Responding with username: {}", createdUser.getUsername());
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{username}")
    public ResponseEntity<User> updateUser(@PathVariable String username, @RequestBody User user) {
        LOGGER.info("Received request to update user with username: {} with data {}", username, user);
        User updatedUser = userService.saveUser(user);
        LOGGER.info("Updated user with username: {}", updatedUser.getUsername());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        LOGGER.info("Received request to delete user with username: {}", username);
        userService.deleteUser(username);
        LOGGER.info("Deleted user with username: {}", username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{username}/delete")
    public ResponseEntity<Void> deleteUserAndTasks(@PathVariable String username) {
        LOGGER.info("Received request to delete user with username: {} and all the their tasks", username);
        userService.deleteUserAndTasks(username);
        LOGGER.info("Deleted user with username: {} and all their tasks", username);
        userService.deleteUser(username); // doubt about double controller reset/delete, should be 1 with keys
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{username}/reset")
    public ResponseEntity<User> resetTimeLogsAndTasks(@PathVariable String username) {
        LOGGER.info("Received request to delete user with username: {} and all the their tasks", username);
        userService.deleteUserAndTasks(username);
        LOGGER.info("Deleted user with username: {} and all their tasks", username);
        User user = userService.getUser(username);
        return ResponseEntity.ok(user);
    }

}
