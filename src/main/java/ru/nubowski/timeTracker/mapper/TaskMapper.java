package ru.nubowski.timeTracker.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.dto.AllTasksGetResponse;
import ru.nubowski.timeTracker.dto.TaskCreateRequest;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.service.impl.UserService;

@Service
public class TaskMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskMapper.class);

    private final UserService userService;

    public TaskMapper(UserService userService) {
        this.userService = userService;
    }


    public Task mapToTask (TaskCreateRequest request, String username) {
        Task task = new Task();
        task.setUser(userService.getUserByUsernameNotOptional(username));
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        // set other..
        return task;
    }
    public AllTasksGetResponse taskToAllTasksGetResponse(Task task) {

        AllTasksGetResponse response = new AllTasksGetResponse(task);
        response.setId(task.getId());
        response.setName(task.getName());
        response.setDescription(task.getDescription());
        response.setCreatedAt(task.getCreatedAt());
        response.setUsername(task.getUser().getUsername());
        return response;
    }
}
