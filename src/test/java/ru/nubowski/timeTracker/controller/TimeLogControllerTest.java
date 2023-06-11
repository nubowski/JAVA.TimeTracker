package ru.nubowski.timeTracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nubowski.timeTracker.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.TimeLog;
import ru.nubowski.timeTracker.service.TaskService;
import ru.nubowski.timeTracker.service.TimeLogService;
import ru.nubowski.timeTracker.service.UserService;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TimeLogControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeLogControllerTest.class);
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TimeLogService timeLogService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;

    @Transactional
    @Test
    void testOnGoingTaskTimeLeft () throws Exception {
        // manyToOne to User column key
        User user = new User();
        user.setUsername("test");
        user = userService.saveUser(user);  // save user first before setting it to task

        Task newTask = new Task();
        // some data
        newTask.setName("testTask");
        newTask.setDescription("Some description to be sure it is working OK");
        newTask.setUser(user);  //  user persisted entity
        Task createdTask = taskService.saveTask(newTask);

        LOGGER.info("About to test task start with task {}", createdTask.getId());

        // request to start
        mockMvc.perform(post("/time_logs/start/" + createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        // simulate the passage of time
        Thread.sleep(5000);

        Duration durationElapsed = timeLogService.getTaskTimeElapsed(createdTask);
        LOGGER.info("Duration elapsed for the task: {}", durationElapsed.toMillis());

        // request to stop
        mockMvc.perform(post("/time_logs/stop/" + createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        TimeLog stoppedTimeLog = timeLogService.getLastTimeLogForTask(createdTask);
        assertNotNull(stoppedTimeLog.getEndTime());

        Duration duration = Duration.between(stoppedTimeLog.getStartTime(), stoppedTimeLog.getEndTime());
        LOGGER.info("Total duration of the task: {}", duration.toMillis());
    }

    @Transactional
    @Test
    void testStartAndStopTask() throws Exception {
        // create new user too, coz manyToOne column key
        User user = new User();
        user.setUsername("test");
        user = userService.saveUser(user);  // saved before !

        Task newTask = new Task();
        // some data
        newTask.setName("testTask");
        newTask.setDescription("Some description to be sure it is working OK");
        newTask.setUser(user);  //  user persisted entity
        Task createdTask = taskService.saveTask(newTask);

        LOGGER.info("About to test task start with task {}", createdTask.getId());

        // request to start
        mockMvc.perform(post("/time_logs/start/" + createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        // simulate the passage of time
        Thread.sleep(5000);

        Duration durationElapsedAfterStart = timeLogService.getTaskTimeElapsed(createdTask);
        LOGGER.info("Duration elapsed after start: {}", durationElapsedAfterStart.toMillis());

        // request to pause
        mockMvc.perform(post("/time_logs/pause/" + createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // simulate the passage of time while the task is paused
        Thread.sleep(5000);

        Duration durationElapsedAfterPause = timeLogService.getTaskTimeElapsed(createdTask);
        LOGGER.info("Duration elapsed after pause: {}", durationElapsedAfterPause.toMillis());

        // request to stop
        mockMvc.perform(post("/time_logs/stop/" + createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        TimeLog stoppedTimeLog = timeLogService.getLastTimeLogForTask(createdTask);
        assertNotNull(stoppedTimeLog.getEndTime());

        Duration duration = Duration.between(stoppedTimeLog.getStartTime(), stoppedTimeLog.getEndTime());
        LOGGER.info("Total duration of the task: {}", duration.toMillis());
    }
}
