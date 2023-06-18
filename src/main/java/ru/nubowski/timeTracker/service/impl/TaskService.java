package ru.nubowski.timeTracker.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.dto.request.TaskCreateRequest;
import ru.nubowski.timeTracker.exception.OngoingTaskNotFoundException;
import ru.nubowski.timeTracker.exception.TaskNotFoundException;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.TaskState;
import ru.nubowski.timeTracker.model.TimeLog;
import ru.nubowski.timeTracker.repository.TaskRepository;
import ru.nubowski.timeTracker.repository.TimeLogRepository;
import ru.nubowski.timeTracker.util.ClockProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    private static final  Logger LOGGER = LoggerFactory.getLogger(TaskService.class);
    private final ClockProvider clockProvider;
    private final TaskRepository taskRepository;
    private final TimeLogRepository timeLogRepository;


    public TaskService(ClockProvider clockProvider, TaskRepository taskRepository, TimeLogRepository timeLogRepository) {
        this.clockProvider = clockProvider;
        this.taskRepository = taskRepository;
        this.timeLogRepository = timeLogRepository;
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
        task.setCreatedAt(LocalDateTime.now());
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

    public TimeLog startTask(Task task) {
        LOGGER.info("Starting a task: {}", task.getId());
        TimeLog timeLog = new TimeLog();
        timeLog.setTask(task);
        timeLog.setStartTime(clockProvider.now());
        timeLog.setTaskState(TaskState.ONGOING);
        return timeLogRepository.save(timeLog);
    }

    public TimeLog stopTask(Task task) {
        LOGGER.info("Stopping task: {}", task.getId());
        TimeLog timeLog = timeLogRepository.findFirstByTaskAndEndTimeIsNullOrderByStartTimeDesc(task). // perfect naming ^-^
                orElseThrow(() -> new OngoingTaskNotFoundException(task.getId()));
        timeLog.setEndTime(clockProvider.now());
        timeLog.setTaskState(TaskState.USER_STOPPED);
        return timeLogRepository.save(timeLog);
    }

    public TimeLog resumeTask(Task task) {
        TimeLog timeLog = timeLogRepository.findByTaskAndTaskState(task, TaskState.PAUSED).
                orElseThrow(() -> new OngoingTaskNotFoundException(task.getId()));
        timeLog.setTaskState(TaskState.USER_STOPPED);
        LOGGER.info("Resuming task: {}", task.getId());
        return startTask(task);
    }

    public TimeLog pauseTask(Task task) {
        LOGGER.info("Pausing task: {}", task.getId());
        TimeLog timeLog = timeLogRepository.findFirstByTaskAndEndTimeIsNullOrderByStartTimeDesc(task).
                orElseThrow(() -> new OngoingTaskNotFoundException(task.getId()));
        timeLog.setEndTime(clockProvider.now());
        timeLog.setTaskState(TaskState.PAUSED);
        return timeLogRepository.save(timeLog);
    }

    public Task updateTask(Long id, TaskCreateRequest request) {
        Task taskToUpdate = taskRepository.findById(id).get(); // fixed doubling, and now .get without isPresent -_-
        taskToUpdate.setName(request.getName());
        taskToUpdate.setDescription(request.getDescription());
        taskRepository.save(taskToUpdate);
        LOGGER.info("Task with id {} is saved", taskToUpdate.getId());
        return taskToUpdate;
    }

    public boolean taskIsPresent(Long id) {
        Optional<Task> checkTask = taskRepository.findById(id);
        return checkTask.isPresent();
    }
}
