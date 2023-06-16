package ru.nubowski.timeTracker.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nubowski.timeTracker.dto.AllTasksGetResponse;
import ru.nubowski.timeTracker.dto.TaskCreateRequest;
import ru.nubowski.timeTracker.dto.TaskCreateResponse;
import ru.nubowski.timeTracker.mapper.TaskMapper;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.service.impl.TaskService;
import ru.nubowski.timeTracker.service.impl.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private static final  Logger LOGGER = LoggerFactory.getLogger(TaskController.class);
    private final TaskMapper taskMapper;
    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskMapper taskMapper, TaskService taskService, UserService userService) {
        this.taskMapper = taskMapper;
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<AllTasksGetResponse>> getAllTasks() {
        LOGGER.info("Received request to get all tasks");
        List<Task> tasks = taskService.getAllTasks();
        List<AllTasksGetResponse> responses = tasks.stream()
                .map(taskMapper::taskToAllTasksGetResponse)
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

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        LOGGER.info("Received request to update task with id: {} with data: {}", id, task);
        task.setId(id);
        Task updatedTask = taskService.saveTask(task);
        LOGGER.info("Updated task with id: {}", updatedTask.getId());
        return ResponseEntity.ok(updatedTask);
    }

    @PostMapping("/{username}")
    public ResponseEntity<TaskCreateResponse> createTask(@PathVariable String username, @Valid @RequestBody TaskCreateRequest request) {
        LOGGER.info("Received request to create task: {} attached to the user {}", request.getName(), username);
        if (!userService.userIsPresent(username)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Task createdTask = taskMapper.mapToTask(request, username);
        LOGGER.info("Created task with id: {}", createdTask.getId());
        return new ResponseEntity<>(new TaskCreateResponse(createdTask), HttpStatus.CREATED);
    }

    @PutMapping("/{username}/{taskName}")
    public ResponseEntity<TaskCreateResponse> updateTask(@PathVariable String username, @PathVariable String taskName, @RequestBody TaskCreateRequest request) {
        LOGGER.info("Received request to update task with username: {} and taskName: {}", username, taskName);
        if (!taskService.taskIsPresent(username, taskName)) {
            LOGGER.error("Task not found with username: {} and taskName: {}", username, taskName);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Task updatedTask = taskService.updateTask(username, taskName, request);
            LOGGER.info("Updated task with id: {}", updatedTask.getId());
            return new ResponseEntity<>(new TaskCreateResponse(updatedTask), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        LOGGER.info("Received request to delete task with id: {}", id);
        taskService.deleteTask(id);
        LOGGER.info("Deleted task with id: {}", id);
        return ResponseEntity.noContent().build();
    }

    /*@PostMapping("/start/{taskName}")
    public ResponseEntity<TimeLog> startTask(@PathVariable String username ,String taskName) {
        LOGGER.info("Received request to start task {}", taskName);
        try {
            Task task = taskService.getTaskByName(username, taskName);
            TimeLog timeLog = taskService.startTask(task);
            LOGGER.info("Task {} has been started", taskName);  // don't think its necessary coz of custom ex, but just in case
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("Error occurred while starting task with id {}", taskName, e);
            throw e;
        }
    }

    @PostMapping("/stop/{taskName}")
    public ResponseEntity<?> stopTask(@PathVariable String taskName) {
        LOGGER.info("Received request to stop task {}", taskName);
        Task task = taskService.getTaskByName(taskName);
        TimeLog timeLog = taskService.stopTask(task);
        LOGGER.info("Task {} has been stopped", taskName);
        return ResponseEntity.ok(HttpStatus.OK);
    }*/
}
