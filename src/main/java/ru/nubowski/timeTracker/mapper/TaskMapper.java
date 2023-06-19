package ru.nubowski.timeTracker.mapper;

import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.dto.TaskToResponse;
import ru.nubowski.timeTracker.dto.request.TaskCreateRequest;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.service.impl.UserService;

/**
 * Mapper service for Task related objects.
 */
@Service
public class TaskMapper {
    private final UserService userService; // TODO: rework and try to kick userService out

    /**
     * Constructor for the TaskMapper.
     *
     * @param userService the UserService
     */
    public TaskMapper(UserService userService) {
        this.userService = userService;
    }

    /**
     * Maps a TaskCreateRequest and a username to a Task object.
     *
     * @param request the TaskCreateRequest
     * @param username the username of the user who created the task
     * @return the created Task object
     */
    public Task mapToTask (TaskCreateRequest request, String username) {
        Task task = new Task();
        task.setUser(userService.getUserByUsernameNotOptional(username));
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        return task;
    }
    /**
     * Maps a Task object to a TaskToResponse object.
     *
     * @param task the Task object
     * @return the created TaskToResponse object
     */
    public TaskToResponse mapTaskToResponse(Task task) {
        // we don't need setter yet. constructor did all the things.
        // but left them to keep in mind for opportunity to change things AFTER constructor
        TaskToResponse response = new TaskToResponse(task);
        response.setId(task.getId());
        response.setName(task.getName());
        response.setDescription(task.getDescription());
        response.setCreatedAt(task.getCreatedAt());
        response.setUsername(task.getUser().getUsername());
        return response;
    }
}
