package ru.nubowski.timeTracker.service;

import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.repository.TaskRepository;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTask(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask (Long id) {
        taskRepository.deleteById(id);
    }
}
