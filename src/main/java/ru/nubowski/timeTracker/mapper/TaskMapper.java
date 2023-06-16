package ru.nubowski.timeTracker.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.dto.TaskToResponse;
import ru.nubowski.timeTracker.dto.TaskCreateRequest;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.service.impl.TaskService;
import ru.nubowski.timeTracker.service.impl.UserService;

@Service
public class TaskMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskMapper.class);

    private final UserService userService;
    private final TaskService taskService;

    public TaskMapper(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    public Task mapToTask (TaskCreateRequest request, String username) {
        Task task = new Task();
        task.setUser(userService.getUserByUsernameNotOptional(username));
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        return task;
    }
    public TaskToResponse mapTaskToResponse(Task task) {
        // we don't need setter yet. constructor did all the thing.
        // but left them to keep in mind to opportunity change things AFTER constructor
        TaskToResponse response = new TaskToResponse(task);
        response.setId(task.getId());
        response.setName(task.getName());
        response.setDescription(task.getDescription());
        response.setCreatedAt(task.getCreatedAt());
        response.setUsername(task.getUser().getUsername());
        return response;
    }
}
