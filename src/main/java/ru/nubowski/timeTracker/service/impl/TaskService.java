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

/**
 * Service for managing tasks.
 */
@Service
public class TaskService {
    private static final  Logger LOGGER = LoggerFactory.getLogger(TaskService.class);
    private final ClockProvider clockProvider;
    private final TaskRepository taskRepository;
    private final TimeLogRepository timeLogRepository;


    /**
     * Constructor for TaskService.
     *
     * @param clockProvider       custom provider to get the current time
     * @param taskRepository      repository for handling tasks
     * @param timeLogRepository   repository for handling time logs
     */
    public TaskService(ClockProvider clockProvider, TaskRepository taskRepository, TimeLogRepository timeLogRepository) {
        this.clockProvider = clockProvider;
        this.taskRepository = taskRepository;
        this.timeLogRepository = timeLogRepository;
    }

    /**
     * Gets a list of all tasks.
     *
     * @return the list of all tasks
     */
    public List<Task> getAllTasks() {
        LOGGER.info("Getting all tasks");
        return taskRepository.findAll();
    }

    /**
     * Gets a task by its id.
     *
     * @param id  the id of the task
     * @return the task with the specified id
     * @throws TaskNotFoundException if the task is not found
     */
    public Task getTask(Long id) {
        LOGGER.debug("Getting task with id: {}", id); // TODO: make some notes of debug/info statements, low view
        return taskRepository.findById(id).
                orElseThrow(() -> new TaskNotFoundException(id));
    }

    /**
     * Saves a new task.
     *
     * @param task the task to be saved
     * @return the saved task
     */
    public Task saveTask(Task task) {
        LOGGER.info("Saving task with id: {}", task.getId());
        task.setCreatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    /**
     * Deletes a task by its id.
     *
     * @param id the id of the task to be deleted
     */
    public void deleteTask (Long id) {
        LOGGER.info("Deleting task with id: {}", id);
        taskRepository.deleteById(id);
    }

    /**
     * Deletes tasks created before the specified date.
     *
     * @param cutoff the date threshold for task deletion
     */
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

    /**
     * Starts a task and creates a new time log.
     *
     * @param task the task to be started
     * @return the created time log
     */
    public TimeLog startTask(Task task) {
        LOGGER.info("Starting a task: {}", task.getId());
        TimeLog timeLog = new TimeLog();
        timeLog.setTask(task);
        timeLog.setStartTime(clockProvider.now());
        timeLog.setTaskState(TaskState.ONGOING);
        return timeLogRepository.save(timeLog);
    }

    /**
     * Stops a task and updates the corresponding time log.
     *
     * @param task the task to be stopped
     * @return the updated time log
     * @throws OngoingTaskNotFoundException if there is no ongoing task
     */
    public TimeLog stopTask(Task task) {
        LOGGER.info("Stopping task: {}", task.getId());
        TimeLog timeLog = timeLogRepository.findFirstByTaskAndEndTimeIsNullOrderByStartTimeDesc(task). // perfect naming ^-^
                orElseThrow(() -> new OngoingTaskNotFoundException(task.getId()));
        timeLog.setEndTime(clockProvider.now());
        timeLog.setTaskState(TaskState.USER_STOPPED);
        return timeLogRepository.save(timeLog);
    }

    /**
     * Resumes a task and creates a new time log.
     * It is mostly for tag a task with USER_STOPPED and make a new one after PAUSE
     *
     * @param task the task to be resumed
     * @return the created time log
     * @throws OngoingTaskNotFoundException if there is no ongoing task
     */
    public TimeLog resumeTask(Task task) {
        TimeLog timeLog = timeLogRepository.findByTaskAndTaskState(task, TaskState.PAUSED).
                orElseThrow(() -> new OngoingTaskNotFoundException(task.getId()));
        timeLog.setTaskState(TaskState.USER_STOPPED);
        LOGGER.info("Resuming task: {}", task.getId());
        return startTask(task);
    }

    /**
     * Pauses a task and change its status to PAUSE.
     * It is mostly for tag a task with PAUSED and not closed, so make a task not CLOSED
     *
     * @param task the task to be paused
     * @return save the time log statement
     */
    public TimeLog pauseTask(Task task) {
        LOGGER.info("Pausing task: {}", task.getId());
        TimeLog timeLog = timeLogRepository.findFirstByTaskAndEndTimeIsNullOrderByStartTimeDesc(task).
                orElseThrow(() -> new OngoingTaskNotFoundException(task.getId()));
        timeLog.setEndTime(clockProvider.now());
        timeLog.setTaskState(TaskState.PAUSED);
        return timeLogRepository.save(timeLog);
    }

    /**
     * Find a task by ID and update its fields
     *
     * @param request DTO of the task from TaskCreateRequest with needed fields of view
     * @return updated task
     */
    public Task updateTask(Long id, TaskCreateRequest request) {
        Task taskToUpdate = taskRepository.findById(id).get(); // fixed doubling, and now .get without isPresent -_-
        taskToUpdate.setName(request.getName());
        taskToUpdate.setDescription(request.getDescription());
        taskRepository.save(taskToUpdate);
        LOGGER.info("Task with id {} is saved", taskToUpdate.getId());
        return taskToUpdate;
    }

    /**
     * Check if task is present.
     *
     * @param id the id of the checked task
     * @return TRUE if it is present and FALSE if not
     */
    public boolean taskIsPresent(Long id) {
        Optional<Task> checkTask = taskRepository.findById(id);
        return checkTask.isPresent();
    }
}
