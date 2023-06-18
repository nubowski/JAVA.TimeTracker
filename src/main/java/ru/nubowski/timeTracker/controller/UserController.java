package ru.nubowski.timeTracker.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nubowski.timeTracker.dto.UserCreateRequest;
import ru.nubowski.timeTracker.dto.UserUpdateRequest;
import ru.nubowski.timeTracker.mapper.UserMapper;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.TimeLog;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.service.ProcessService;
import ru.nubowski.timeTracker.service.impl.TimeLogService;
import ru.nubowski.timeTracker.service.impl.UserService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final UserMapper userMapper;
    private final ProcessService processService;
    private final TimeLogService timeLogService;

    public UserController(UserService userService, UserMapper userMapper, ProcessService processService, TimeLogService timeLogService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.processService = processService;
        this.timeLogService = timeLogService;
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
        if (userService.userIsPresent(request.getUsername())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // if user doesn't exist
        User userToCreate = userMapper.mapToUser(request);
        User createdUser = userService.saveUser(userToCreate);
        LOGGER.info("Created user: {}", createdUser.getUsername());
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{username}") // TODO: all is good until username is unique entity, if not - auth/auth/regis approach
    public ResponseEntity<User> updateUser(@PathVariable String username, @Valid @RequestBody UserUpdateRequest request) {
        LOGGER.info("Received request to update user with username: {} with data {}", username, request);
        if (!userService.userIsPresent(username)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User userToUpdate = userMapper.mapToUpgradeUser(request, username);
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
        processService.deleteTimeLogsAndTasks(username);
        userService.deleteUser(username); // doubt about double controller reset/delete, should be 1 with keys
        LOGGER.info("Deleted user with username: {} and all their tasks", username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{username}/reset")
    public ResponseEntity<User> resetTimeLogsAndTasks(@PathVariable String username) {
        LOGGER.info("Received request to delete user with username: {} and all the their tasks", username);
        processService.deleteTimeLogsAndTasks(username);
        LOGGER.info("Deleted user with username: {} and all their tasks", username);
        User user = userService.getUser(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{username}/time_logs/date_range")
    public ResponseEntity<List<String>> getTimeLogsByUserAndDateRange(
            @PathVariable String username,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(value = "sort", required = false, defaultValue = "duration") String sort,
            @RequestParam(value = "output", required = false, defaultValue = "duration") String output) {

        LOGGER.info("Fetching time logs for user {} in date range from {} to {}", username, start, end);
        User user = userService.getUser(username);
        List<TimeLog> timeLogs = timeLogService.getTimeLogsByUserAndDateRange(user, start, end);
        List<Map.Entry<Task, Long>> sortedTimeLogs = timeLogService.sortTimeLogs(timeLogs, sort, end);
        List<String> formattedTimeLogs = timeLogService.formatTimeLogs(timeLogs, sortedTimeLogs, output);
        LOGGER.info("Fetched {} time logs for user {} in date range from {} to {}", formattedTimeLogs.size(), username, start, end);
        return ResponseEntity.ok(formattedTimeLogs);
    }

    // only completed TimeLogs, TODO: add a method for check and add ongoing tasks too
    @GetMapping("{username}/work_effort")
    public ResponseEntity<String> getTotalWorkEffortByUserAndDateRange(
            @PathVariable String username,
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end) {

        LOGGER.info("Calculating total work effort for user {} in date range from {} to {}", username, start, end);
        User user = userService.getUser(username);
        Duration totalWorkEffort = timeLogService.getTotalWorkEffortByUserAndDataRange(user, start, end);
        String formattedTotalWorkEffort = String.format("%02d:%02d", totalWorkEffort.toHours(), totalWorkEffort.toMinutesPart());
        LOGGER.info("Total work effort for user {} in date range from {} to {} is {}", username, start, end, formattedTotalWorkEffort);
        return ResponseEntity.ok(formattedTotalWorkEffort);
    }

}
