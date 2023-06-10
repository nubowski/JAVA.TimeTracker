package ru.nubowski.timeTracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.exception.TaskNotFoundException;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {
    private final static Logger LOGGER = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        LOGGER.info("Getting all tasks");
        return taskRepository.findAll();
    }

    public Task getTask(Long id) {
        LOGGER.debug("Getting task with id: {}", id); // TODO: make some notes of debug/info statements, low view
        return taskRepository.findById(id).
                orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task saveTask(Task task) {
        LOGGER.info("Saving task with id: {}", task.getId());
        return taskRepository.save(task);
    }

    public void deleteTask (Long id) {
        LOGGER.info("Deleting task with id: {}", id);
        taskRepository.deleteById(id);
    }

    public void deleteOldTasks(LocalDateTime cutoff) {
        LOGGER.debug("Deleting tasks created before: {}", cutoff);
        List<Task> oldTasks = taskRepository.findByCreatedAtBefore(cutoff);
        if(!oldTasks.isEmpty()) {
            taskRepository.deleteAll(oldTasks);
            LOGGER.info("{} old tasks deleted successfully", oldTasks.size());
        } else {
            LOGGER.info("No old tasks to delete");
        }
    }
}
