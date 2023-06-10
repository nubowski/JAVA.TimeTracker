package ru.nubowski.timeTracker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private static final  Logger LOGGER = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        LOGGER.info("Received request to get all tasks");
        List<Task> tasks = taskService.getAllTasks();
        LOGGER.info("Responding with {} tasks", tasks.size());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        LOGGER.info("Received request to get task with id: {}", id);
        Task task = taskService.getTask(id);
        LOGGER.info("Responding with task: {}", task);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        LOGGER.info("Received request to create task: {}", task);
        Task createdTask = taskService.saveTask(task);
        LOGGER.info("Created task with id: {}", createdTask.getId());
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        LOGGER.info("Received request to update task with id: {} with data: {}", id, task);
        task.setId(id);
        Task updatedTask = taskService.saveTask(task);
        LOGGER.info("Updated task with id: {}", updatedTask.getId());
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        LOGGER.info("Received request to delete task with id: {}", id);
        taskService.deleteTask(id);
        LOGGER.info("Deleted task with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
