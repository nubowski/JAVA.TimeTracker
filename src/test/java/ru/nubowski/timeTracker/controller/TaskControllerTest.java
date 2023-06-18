package ru.nubowski.timeTracker.controller;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import ru.nubowski.timeTracker.model.TaskState;
import ru.nubowski.timeTracker.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.TimeLog;
import ru.nubowski.timeTracker.service.impl.TaskService;
import ru.nubowski.timeTracker.service.impl.TimeLogService;
import ru.nubowski.timeTracker.service.impl.UserService;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskControllerTest.class);
    @Autowired
    private MockMvc mockMvc;
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
        mockMvc.perform(post("/tasks/start/" + createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isCreated());

        // simulate the passage of time
        Thread.sleep(5000); // minus days minutes millis

        Duration durationElapsed = timeLogService.getTaskTimeElapsed(createdTask);
        LOGGER.info("Duration elapsed for the task: {}", durationElapsed.toMillis());

        // request to stop
        mockMvc.perform(post("/tasks/stop/" + createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());

        TimeLog stoppedTimeLog = timeLogService.getLastTimeLogForTask(createdTask);
        assertNotNull(stoppedTimeLog.getEndTime());

        Duration duration = Duration.between(stoppedTimeLog.getStartTime(), stoppedTimeLog.getEndTime());
        LOGGER.info("Total duration of the task: {}", duration.toMillis());
    }

    @Transactional
    @Test
    void testStartPauseResumeAndStopTask() throws Exception {
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
        mockMvc.perform(post("/tasks/start/" + createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        List<TimeLog> timeLogsAfterStart = timeLogService.getAllTimeLogsForTask(createdTask);
        assertEquals(1, timeLogsAfterStart.size(), "Should be 1 TimeLog after starting the task");
        assertEquals(TaskState.ONGOING, timeLogsAfterStart.get(0).getTaskState(), "Task state should be ONGOING after starting the task");

        // request to pause
        mockMvc.perform(post("/tasks/pause/" + createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        List<TimeLog> timeLogsAfterPause = timeLogService.getAllTimeLogsForTask(createdTask);
        assertEquals(1, timeLogsAfterPause.size(), "Should still be 1 TimeLog after pausing the task");
        assertEquals(TaskState.PAUSED, timeLogsAfterPause.get(0).getTaskState(), "Task state should be PAUSED after pausing the task");

        // request to resume
        mockMvc.perform(post("/tasks/resume/" + createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        List<TimeLog> timeLogsAfterResume = timeLogService.getAllTimeLogsForTask(createdTask);
        assertEquals(2, timeLogsAfterResume.size(), "Should be 2 TimeLogs after resuming the task");
        assertEquals(TaskState.ONGOING, timeLogsAfterResume.get(0).getTaskState(), "Task 1 state should be USER_STOPPED after resuming the task");
        assertEquals(TaskState.USER_STOPPED, timeLogsAfterResume.get(1).getTaskState(), "Task 2 state should be ONGOING after resuming the task");

        // request to stop
        mockMvc.perform(post("/tasks/stop/" + createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        List<TimeLog> timeLogsAfterStop = timeLogService.getAllTimeLogsForTask(createdTask);
        assertEquals(2, timeLogsAfterStop.size(), "Should still be 2 TimeLogs after stopping the task");
        assertEquals(TaskState.USER_STOPPED, timeLogsAfterStop.get(0).getTaskState(), "Task 1 state should be USER_STOPPED after stopping the task");
        assertEquals(TaskState.USER_STOPPED, timeLogsAfterStop.get(1).getTaskState(), "Task 2 state should be USER_STOPPED after stopping the task");
    }
}
