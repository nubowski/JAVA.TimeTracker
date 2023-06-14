package ru.nubowski.timeTracker.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
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
import ru.nubowski.timeTracker.repository.TimeLogRepository;
import ru.nubowski.timeTracker.service.TaskService;
import ru.nubowski.timeTracker.service.TimeLogService;
import ru.nubowski.timeTracker.service.UserService;
import ru.nubowski.timeTracker.util.CustomClockProvider;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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
    @Autowired
    private CustomClockProvider clockProvider;


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
        Thread.sleep(5000); // minus days minutes millis

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
        mockMvc.perform(post("/time_logs/start/" + createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        List<TimeLog> timeLogsAfterStart = timeLogService.getAllTimeLogsForTask(createdTask);
        assertEquals(1, timeLogsAfterStart.size(), "Should be 1 TimeLog after starting the task");
        assertEquals(TaskState.ONGOING, timeLogsAfterStart.get(0).getTaskState(), "Task state should be ONGOING after starting the task");

        // request to pause
        mockMvc.perform(post("/time_logs/pause/" + createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        List<TimeLog> timeLogsAfterPause = timeLogService.getAllTimeLogsForTask(createdTask);
        assertEquals(1, timeLogsAfterPause.size(), "Should still be 1 TimeLog after pausing the task");
        assertEquals(TaskState.PAUSED, timeLogsAfterPause.get(0).getTaskState(), "Task state should be PAUSED after pausing the task");

        // request to resume
        mockMvc.perform(post("/time_logs/resume/" + createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        List<TimeLog> timeLogsAfterResume = timeLogService.getAllTimeLogsForTask(createdTask);
        assertEquals(2, timeLogsAfterResume.size(), "Should be 2 TimeLogs after resuming the task");
        assertEquals(TaskState.ONGOING, timeLogsAfterResume.get(0).getTaskState(), "Task 1 state should be USER_STOPPED after resuming the task");
        assertEquals(TaskState.USER_STOPPED, timeLogsAfterResume.get(1).getTaskState(), "Task 2 state should be ONGOING after resuming the task");

        // request to stop
        mockMvc.perform(post("/time_logs/stop/" + createdTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        List<TimeLog> timeLogsAfterStop = timeLogService.getAllTimeLogsForTask(createdTask);
        assertEquals(2, timeLogsAfterStop.size(), "Should still be 2 TimeLogs after stopping the task");
        assertEquals(TaskState.USER_STOPPED, timeLogsAfterStop.get(0).getTaskState(), "Task 1 state should be USER_STOPPED after stopping the task");
        assertEquals(TaskState.USER_STOPPED, timeLogsAfterStop.get(1).getTaskState(), "Task 2 state should be USER_STOPPED after stopping the task");
    }

    @Transactional
    @Test
    void testGetAllUserTasksInPeriodSortedByTimeSpent() throws Exception {
        // create user
        User user = new User();
        user.setUsername("time_user");
        user.setDisplayName("nagibator9000");
        user.setEmail("test@mail.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                        .andExpect(status().isCreated());

            // create tasks

            User savedUser = userService.getUserByUsernameNotOptional(user.getUsername());

            Task task1 = new Task();
            task1.setName("testTask1");
            task1.setDescription("Some description to be sure it is working OK");
            task1.setUser(savedUser);
            task1 = taskService.saveTask(task1);

            Task task2 = new Task();
            task2.setName("testTask2");
            task2.setDescription("Some description to be sure it is working OK");
            task2.setUser(savedUser);
            task2 = taskService.saveTask(task2);

            Task task3 = new Task();
            task3.setName("testTask3");
            task3.setDescription("Some description to be sure it is working OK");
            task3.setUser(savedUser);
            task3 = taskService.saveTask(task3);
            // emulate of start and stop tasks
            // could be done by loop, but want to make it as clear as possible

            // task1
            clockProvider.minusTime(Duration.ofHours(10));
            mockMvc.perform(post("/time_logs/start/" + task1.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
            clockProvider.plusTime(Duration.ofMinutes(10));
            mockMvc.perform(post("/time_logs/stop/" + task1.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            clockProvider.resetTime();

            // task2
            clockProvider.minusTime(Duration.ofDays(3));
            mockMvc.perform(post("/time_logs/start/" + task2.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
            clockProvider.plusTime(Duration.ofMinutes(49));
            mockMvc.perform(post("/time_logs/stop/" + task2.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            clockProvider.resetTime();

            // task3
            clockProvider.minusTime(Duration.ofHours(1));
            mockMvc.perform(post("/time_logs/start/" + task3.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
            clockProvider.resetTime();
            LOGGER.info("Tasks are created and initiated");

        // get all tasks for user in a period
        MvcResult mvcResult = mockMvc.perform(get("/time_logs/user/{username}/date_range?start={start}&end={end}",
                        "time_user",
                        LocalDateTime.now().minusDays(1).toString(),
                        LocalDateTime.now().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();
        // extract
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<String> returnedTasks = objectMapper.readValue(contentAsString, new TypeReference<>() {});
        LOGGER.info("Returned tasks: {}", returnedTasks);

        List<String> expectedTasks = List.of(
                String.format("%s - 01:00", task3.getName()),
                String.format("%s - 00:10", task1.getName())
        );

        assertEquals(expectedTasks, returnedTasks);
    }
}
