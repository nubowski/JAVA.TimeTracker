package ru.nubowski.timeTracker.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nubowski.timeTracker.dto.TaskToResponse;
import ru.nubowski.timeTracker.dto.request.TaskCreateRequest;
import ru.nubowski.timeTracker.dto.response.TaskCreateResponse;
import ru.nubowski.timeTracker.dto.response.TaskStateResponse;
import ru.nubowski.timeTracker.mapper.TaskMapper;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.TimeLog;
import ru.nubowski.timeTracker.service.impl.TaskService;
import ru.nubowski.timeTracker.service.impl.TimeLogService;
import ru.nubowski.timeTracker.service.impl.UserService;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private static final  Logger LOGGER = LoggerFactory.getLogger(TaskController.class);
    private final TaskMapper taskMapper;
    private final TaskService taskService;
    private final UserService userService;
    private final TimeLogService timeLogService;

    public TaskController(TaskMapper taskMapper, TaskService taskService, UserService userService, TimeLogService timeLogService) {
        this.taskMapper = taskMapper;
        this.taskService = taskService;
        this.userService = userService;
        this.timeLogService = timeLogService;
    }

    // seems lise useless as is. should to return .size()
    @GetMapping
    public ResponseEntity<List<TaskToResponse>> getAllTasks() {
        LOGGER.info("Received request to get all tasks");
        List<Task> tasks = taskService.getAllTasks();
        List<TaskToResponse> responses = tasks.stream()
                .map(taskMapper::mapTaskToResponse)
                .collect(Collectors.toList());
        LOGGER.info("Responding with {} tasks", responses.size());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        LOGGER.info("Received request to get task with id: {}", id);
        Task task = taskService.getTask(id);
        LOGGER.info("Responding with task: {}", task);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/{username}")
    public ResponseEntity<TaskCreateResponse> createTask(@PathVariable String username, @Valid @RequestBody TaskCreateRequest request) {
        LOGGER.info("Received request to create task: {} attached to the user {}", request.getName(), username);
        if (!userService.userIsPresent(username)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Task createdTask = taskMapper.mapToTask(request, username);
        taskService.saveTask(createdTask);
        LOGGER.info("Created task with id: {}", createdTask.getId());
        return new ResponseEntity<>(new TaskCreateResponse(createdTask), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskCreateResponse> updateTask(@PathVariable Long id, @RequestBody TaskCreateRequest request) {
        LOGGER.info("Received request to update task with id: {}", id);
        if (!taskService.taskIsPresent(id)) {
            LOGGER.error("Task not found with id: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Task updatedTask = taskService.updateTask(id, request);
            LOGGER.info("Updated task with id: {}", updatedTask.getId());
            return new ResponseEntity<>(new TaskCreateResponse(updatedTask), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        LOGGER.info("Received request to delete task with id: {}", id);
        taskService.deleteTask(id);
        LOGGER.info("Deleted task with id: {}", id);
        return ResponseEntity.noContent().build(); // 204 is OK or feedback?
    }

    @PostMapping("/start/{taskId}")
    public ResponseEntity<TaskStateResponse> startTask(@PathVariable Long taskId) {
        LOGGER.info("Received request to start task with id {}", taskId);
        try {
            Task task = taskService.getTask(taskId);
            TimeLog timeLog = taskService.startTask(task);
            LOGGER.info("Task with id {} has been started", taskId);  // don't think its necessary coz of custom ex, but just in case
            TaskStateResponse responseDTO = new TaskStateResponse(timeLog);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            LOGGER.error("Error occurred while starting task with id {}", taskId, e);
            throw e;
        }
    }

    @PostMapping("/stop/{taskId}")
    public ResponseEntity<TaskStateResponse> stopTask(@PathVariable Long taskId) {
        LOGGER.info("Received request to stop task with id {}", taskId);
        Task task = taskService.getTask(taskId);
        TimeLog timeLog = taskService.stopTask(task);
        LOGGER.info("Task with id {} has been stopped", taskId);
        TaskStateResponse responseDTO = new TaskStateResponse(timeLog);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/pause/{taskId}")
    public ResponseEntity<TaskStateResponse> pauseTask(@PathVariable Long taskId) {
        LOGGER.info("Received request to pause task with id {}", taskId);
        Task task = taskService.getTask(taskId);
        TimeLog timeLog = taskService.pauseTask(task);
        LOGGER.info("Task with id {} has been paused", taskId);
        TaskStateResponse responseDTO = new TaskStateResponse(timeLog);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/resume/{taskId}")
    public ResponseEntity<TaskStateResponse> resumeTask(@PathVariable Long taskId) {
        LOGGER.info("Received request to resume task with id {}", taskId);
        Task task = taskService.getTask(taskId);
        TimeLog timeLog = taskService.resumeTask(task);
        LOGGER.info("Task with id {} has been resumed", taskId);
        TaskStateResponse responseDTO = new TaskStateResponse(timeLog);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{taskId}/time_elapsed")
    public ResponseEntity<Duration> getTaskTimeElapsed(@PathVariable Long taskId) {
        LOGGER.info("Received request to get time elapsed for task with id {}", taskId);
        Task task = taskService.getTask(taskId);
        Duration timeElapsed = timeLogService.getTaskTimeElapsed(task);
        LOGGER.info("Time elapsed for task with id {} is {}", taskId, timeElapsed);
        return ResponseEntity.ok(timeElapsed);
    }
}
