package ru.nubowski.timeTracker.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nubowski.timeTracker.dto.request.UserCreateRequest;
import ru.nubowski.timeTracker.dto.request.UserUpdateRequest;
import ru.nubowski.timeTracker.dto.response.UsersGetResponse;
import ru.nubowski.timeTracker.mapper.UserMapper;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.TimeLog;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.service.ProcessService;
import ru.nubowski.timeTracker.service.impl.TimeLogService;
import ru.nubowski.timeTracker.service.impl.UserService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for handling user-related endpoints.
 */
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

    /**
     * Returns all users.
     *
     * @return a list of all users.
     */
    @GetMapping
    public ResponseEntity<List<UsersGetResponse>> getAllUsers() {
        LOGGER.info("Received request to get all users");
        List<User> users = userService.getAllUsers();
        List<UsersGetResponse> userDTOs = users.stream()
                .map(userMapper::mapToUserGetResponse)
                .collect(Collectors.toList());
        LOGGER.info("Responding with {} users", userDTOs.size());
        return ResponseEntity.ok(userDTOs);
    }

    /**
     * Returns the user with the given username.
     *
     * @param username the username of the user to retrieve.
     * @return the user with the given username.
     */
    @GetMapping("/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        LOGGER.info("Received request to get user with username: {}", username);
        User user = userService.getUser(username);
        LOGGER.info("Responding with user {}", user);
        return ResponseEntity.ok(user);
    }

    /**
     * Creates a new user.
     *
     * @param request the details of the user to be created.
     * @return the created user.
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = UsersGetResponse.class)))})
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

    /**
     * Updates the user with the given username.
     *
     * @param username the username of the user to be updated.
     * @param request the new details of the user.
     * @return the updated user.
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = UsersGetResponse.class)))})
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

    /**
     * Deletes the user with the given username.
     * NOT IMPLEMENTED YET
     *
     * @param username the username of the user to be deleted.
     * @return HTTP status 503.
     */
    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Feature not yet implemented");
    }

    /**
     * Deletes the user and their tasks with the given username.
     *
     * @param username the username of the user and their tasks to be deleted.
     * @return a response entity with HTTP status 204.
     */
    @DeleteMapping("/{username}/delete")
    public ResponseEntity<Void> deleteUserAndTasks(@PathVariable String username) {
        LOGGER.info("Received request to delete user with username: {} and all the their tasks", username);
        processService.deleteTimeLogsAndTasks(username);
        userService.deleteUser(username); // doubt about double controller reset/delete, should be 1 with keys
        LOGGER.info("Deleted user with username: {} and all their tasks", username);
        return ResponseEntity.noContent().build(); // maybe add some feedback with string or 204 is enough
    }

    /**
     * Deletes all the tasks and time logs of the user with the given username.
     *
     * @param username the username of the user whose tasks and time logs are to be reset.
     * @return the user after the reset.
     */
    @DeleteMapping("/{username}/reset")
    public ResponseEntity<User> resetTimeLogsAndTasks(@PathVariable String username) {
        LOGGER.info("Received request to delete user with username: {} and all the their tasks", username);
        processService.deleteTimeLogsAndTasks(username);
        LOGGER.info("Deleted user with username: {} and all their tasks", username);
        User user = userService.getUser(username);
        return ResponseEntity.ok(user);
    }

    /**
     * Returns the time logs of the user within the given date range.
     *
     * @param username the username of the user.
     * @param start the start date of the range.
     * @param end the end date of the range.
     * @param sort the sort order of the time logs.
     * @param output the output format of the time logs.
     * @return a list of time logs of the user within the given date range.
     */
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

    /**
     * Returns the total work effort of the user within the given date range.
     *
     * @param username the username of the user.
     * @param start the start date of the range.
     * @param end the end date of the range.
     * @return the total work effort of the user within the given date range.
     */
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
