package ru.nubowski.timeTracker.userCaseTests;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.temporal.ChronoUnit;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import ru.nubowski.timeTracker.controller.TaskControllerTest;
import ru.nubowski.timeTracker.dto.UserCreateRequest;
import ru.nubowski.timeTracker.dto.UserUpdateRequest;
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
import ru.nubowski.timeTracker.util.CustomClockProvider;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserTestCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskControllerTest.class);
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

    // создать пользователя трекинга
    @Transactional
    @Test
    void testUserCreation () throws Exception {
        User user = new User();
        user.setUsername("time_user");
        user.setEmail("user@test.com");
        user.setDisplayName("nagibator9000");

        LOGGER.debug("User with username {} saved", user.getUsername());
        MvcResult mvcResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn();
        LOGGER.debug("User with username {} added to DB", user.getUsername());

        String contentAsString = mvcResult.getResponse().getContentAsString();
        User returnedUser = objectMapper.readValue(contentAsString, User.class);

        assertEquals(user.getUsername(), returnedUser.getUsername());
        assertEquals(user.getEmail(), returnedUser.getEmail());
        assertEquals(user.getDisplayName(), returnedUser.getDisplayName());
    }

    // изменить данные пользователя
    @Transactional
    @Test
    void testUserUpdating () throws Exception {
        // create user
        UserCreateRequest createUserRequest = new UserCreateRequest();
        createUserRequest.setUsername("time_user");
        createUserRequest.setEmail("user@test.com");
        createUserRequest.setDisplayName("nagibator9000");

        // request to create the user
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
        LOGGER.debug("User with username {} added to DB", createdUser.getUsername());

        // update user
        UserUpdateRequest updateUserRequest = new UserUpdateRequest();
        updateUserRequest.setDisplayName("Vladimir Bot"); // updated display name
        updateUserRequest.setEmail("newemail@test.com"); // updated email

        // request to update the user
        MvcResult resultUpdated = mockMvc.perform(put("/users/" + createdUser.getUsername())  // use put request here
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk())
                .andReturn();

        User userAfterUpdate = objectMapper.readValue(resultUpdated.getResponse().getContentAsString(), User.class);
        LOGGER.debug("User with username {} updated", userAfterUpdate.getUsername());

        // assert
        assertEquals("newemail@test.com", userAfterUpdate.getEmail());
        assertEquals("Vladimir Bot", userAfterUpdate.getDisplayName());
    }

    // начать отсчет времени по задаче Х прекратить отсчет времени по задаче Х
    @Transactional
    @Test
    void testUserStartAndStopTask() throws Exception {
        // create user
        User user = new User();
        user.setUsername("time_user");
        user.setEmail("user@test.com");
        user.setDisplayName("nagibator9000");


        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        User savedUser = userService.getUserByUsernameNotOptional(user.getUsername());

        // create task
        Task task = new Task();
        task.setName("testTask1");
        task.setDescription("Some description to be sure it is working OK");
        task.setUser(savedUser);
        taskService.saveTask(task);

        mockMvc.perform(post("/tasks/" + user.getUsername())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated())
                .andReturn();



        // time before starting the task
        LocalDateTime expectedStart = clockProvider.LocalTime();
        clockProvider.resetTime();

        mockMvc.perform(post("/tasks/start/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        Thread.sleep(2000);

        // time after stopping the task and sync with LocalDateTime
        LocalDateTime expectedEnd = clockProvider.LocalTime();
        clockProvider.resetTime();

        mockMvc.perform(post("/tasks/stop/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/time_logs/task/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // extract response
        String content = result.getResponse().getContentAsString();

        // create an ObjectMapper configured to handle Java 8 date/time types
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // deserialize content
        List<TimeLog> timeLogs = objectMapper.readValue(content, new TypeReference<List<TimeLog>>(){});

        // for each TimeLog, compare start and end times with expected times
        for (TimeLog timeLog : timeLogs) {
            LocalDateTime actualStart = timeLog.getStartTime();
            LocalDateTime actualEnd = timeLog.getEndTime();

            // Allow a margin of error (2 seconds in this example)
            long startDiff = ChronoUnit.MILLIS.between(actualStart, expectedStart);
            long endDiff = ChronoUnit.MILLIS.between(actualEnd, expectedEnd);

            LOGGER.info("Expected start time: {} and Received start time: {}", expectedStart, actualStart);
            LOGGER.info("Expected stop time: {} and Received stop time: {}", expectedEnd, actualEnd);

            assertTrue(Math.abs(startDiff) <= 800, "Start time is not within expected range");
            assertTrue(Math.abs(endDiff) <= 800, "End time is not within expected range");
        }
    }

    // показать все трудозатраты пользователя Y за период N..M в виде связного списка Задача - Сумма
    // затраченного времени в виде (чч:мм), сортировка по времени поступления в трекер (для ответа
    // на вопрос, На какие задачи я потратил больше времени)
    // SORT KEY VALUES :  add ?sort=duration / ?sort=start_time, default is sort by duration
    @Transactional
    @Test
    void testGetAllUserTasksInPeriodSortedByKey() throws Exception {
        // create user
        User user = new User();
        user.setUsername("time_user");
        user.setEmail("pupsa@kuksa.com");
        user.setDisplayName("nagibator9000");


        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                        .andExpect(status().isCreated());

        User savedUser = userService.getUserByUsernameNotOptional(user.getUsername());
        // create tasks

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
        mockMvc.perform(post("/tasks/start/" + task1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        clockProvider.plusTime(Duration.ofMinutes(10));
        mockMvc.perform(post("/tasks/stop/" + task1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        clockProvider.resetTime();

        // task2
        clockProvider.minusTime(Duration.ofDays(3));
        mockMvc.perform(post("/tasks/start/" + task2.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        clockProvider.plusTime(Duration.ofMinutes(49));
        mockMvc.perform(post("/tasks/stop/" + task2.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        clockProvider.resetTime();

        // task3
        clockProvider.minusTime(Duration.ofHours(1));
        mockMvc.perform(post("/tasks/start/" + task3.getId())
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

    // показать все временные интервалы занятые работой за период N..M в виде связного списка
    // Временной интервал (число чч:мм) - Задача (для ответа на вопросы, На что ушла моя неделя
    // или Где за прошедшую неделю были ‘дыры’, когда я ничего не делал)
    // SORT KEY VALUES : add ?output=interval
    @Transactional
    @Test
    void testGetAllWorkIntervalsForUserInPeriodByDateRange() throws Exception {
        // create user
        User user = new User();
        user.setUsername("time_user");
        user.setEmail("user@test.com");
        user.setDisplayName("nagibator9000");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        User savedUser = userService.getUserByUsernameNotOptional(user.getUsername());

        // create tasks
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

        // task1
        clockProvider.minusTime(Duration.ofHours(57));
        mockMvc.perform(post("/tasks/start/" + task1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        clockProvider.plusTime(Duration.ofMinutes(257));
        mockMvc.perform(post("/tasks/stop/" + task1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        clockProvider.resetTime();

        // task2
        clockProvider.minusTime(Duration.ofDays(3));
        mockMvc.perform(post("/tasks/start/" + task2.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        clockProvider.plusTime(Duration.ofMinutes(49));
        mockMvc.perform(post("/tasks/stop/" + task2.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        clockProvider.resetTime();

        // TODO: add a TimeLog with PAUSE and ONGOING STATE

        LOGGER.info("Tasks are created and initiated");

        // get all work intervals for user in a period
        MvcResult mvcResult = mockMvc.perform(get("/time_logs/user/{username}/date_range?start={start}&end={end}&output=interval",
                        "time_user",
                        LocalDateTime.now().minusDays(5).toString(),
                        LocalDateTime.now().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // extract
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<String> returnedWorkIntervals = objectMapper.readValue(contentAsString, new TypeReference<List<String>>() {});
        LOGGER.info("Returned work intervals: {}", returnedWorkIntervals);

        // Create expected list of work intervals
        String expectedInterval1 = String.format("%s - %s | %s",
                LocalDateTime.now().minusHours(57).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                LocalDateTime.now().minusHours(57).plusMinutes(257).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                task1.getName());
        String expectedInterval2 = String.format("%s - %s | %s",
                LocalDateTime.now().minusDays(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                LocalDateTime.now().minusDays(3).plusMinutes(49).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                task2.getName());
        List<String> expectedWorkIntervals = List.of(expectedInterval1, expectedInterval2);

        // Check if the expected and returned lists are equal
        assertEquals(expectedWorkIntervals, returnedWorkIntervals);
    }

    // показать сумму трудозатрат по всем задачам пользователя Y за период N..M
    // 1. task startTime and endTime is between start-end (common case)
    //  2. task startTime and endTime bot out of coverage (seldom, but we must handle this)
    //  3. task started before coverage (startTime should be `start` for sum)
    //  4. task closed after coverage (endTime should be `end` then)
    //  5. task is ONGOING and do not have endTime
    //      5.1 task is started before `start`
    //      5.2 task is started between start-end
    @Transactional
    @Test
    void testTotalWorkEffortByUserAndDateRange () throws Exception {
        // create user
        User user = new User();
        user.setUsername("time_user");
        user.setDisplayName("nagibator9000");
        user.setEmail("poshlay@plusha.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());
        clockProvider.resetTime();

        User savedUser = userService.getUserByUsernameNotOptional(user.getUsername());

        // create tasks
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

        Task task4 = new Task();
        task4.setName("testTask4");
        task4.setDescription("Some description to be sure it is working OK");
        task4.setUser(savedUser);
        task4 = taskService.saveTask(task4);

        // task1
        clockProvider.minusTime(Duration.ofHours(26));
        mockMvc.perform(post("/tasks/start/" + task1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        clockProvider.plusTime(Duration.ofMinutes(127));
        mockMvc.perform(post("/tasks/stop/" + task1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        clockProvider.resetTime();

        // task2
        clockProvider.minusTime(Duration.ofDays(3));
        mockMvc.perform(post("/tasks/start/" + task2.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        clockProvider.plusTime(Duration.ofMinutes(49));
        mockMvc.perform(post("/tasks/stop/" + task2.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        clockProvider.resetTime();

        // task3
        clockProvider.minusTime(Duration.ofDays(5));
        mockMvc.perform(post("/tasks/start/" + task3.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        clockProvider.plusTime(Duration.ofMinutes(264));
        mockMvc.perform(post("/tasks/stop/" + task3.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        clockProvider.resetTime();

        // task4
        clockProvider.minusTime(Duration.ofHours(25));
        mockMvc.perform(post("/tasks/start/" + task4.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        clockProvider.resetTime();

        LOGGER.info("Tasks are created and initiated");

        MvcResult mvcResult = mockMvc.perform(get("/time_logs/user/{username}/work_effort?start={start}&end={end}&",
                        "time_user",
                        LocalDateTime.now().minusDays(8).toString(),
                        LocalDateTime.now().minusDays(1).toString())  // for a week with a 1 day before slice
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // total work effort is correct
        String content = mvcResult.getResponse().getContentAsString();
        String expectedWorkEffort = "08:13"; // total in HH:mm format, + task1 and task4 conditions (outOfCoverage + ONGOING)

        assertEquals(expectedWorkEffort, content, "Total work effort was not calculated correctly");
    }

    // удалить всю информацию о пользователе Z
    @Transactional
    @Test
    void testDeleteUser() throws Exception {
        // create user
        User user = new User();
        user.setUsername("delete_user");
        user.setEmail("user@test.com");
        user.setDisplayName("nagibator9000");
        user = userService.saveUser(user);

        // create task
        Task task = new Task();
        task.setName("testTask1");
        task.setDescription("Some description to be sure it is working OK");
        task.setUser(user);
        task = taskService.saveTask(task);


        mockMvc.perform(post("/tasks/start/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        Thread.sleep(500);
        clockProvider.resetTime(); // sync CustomTimeClockProvider

        mockMvc.perform(post("/tasks/stop/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        clockProvider.resetTime();

        // deleting user request
        mockMvc.perform(delete("/users/" + user.getUsername() + "/delete")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // try to get the user
        MvcResult resultUser = mockMvc.perform(get("/users/" + user.getUsername())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        MvcResult resultTask = mockMvc.perform(get("/tasks/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        MvcResult resultTimeLog = mockMvc.perform(get("/time_logs/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // returned status should be 404 (Not Found)
        assertEquals(404, resultTimeLog.getResponse().getStatus(), "TimeLog was not deleted successfully");
        assertEquals(404, resultTask.getResponse().getStatus(), "Task was not deleted successfully");
        assertEquals(404, resultUser.getResponse().getStatus(), "User was not deleted successfully");
    }

    @Transactional
    @Test
    void testResetUserTrackInfo() throws Exception {
        // create user
        User user = new User();
        user.setUsername("reset_user");
        user.setEmail("user@test.com");
        user.setDisplayName("nagibator9000");
        user = userService.saveUser(user);

        // create task
        Task task = new Task();
        task.setName("testTask1");
        task.setDescription("Some description to be sure it is working OK");
        task.setUser(user);
        task = taskService.saveTask(task);


        mockMvc.perform(post("/tasks/start/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        Thread.sleep(500);
        clockProvider.resetTime(); // sync CustomTimeClockProvider

        mockMvc.perform(post("/tasks/stop/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        clockProvider.resetTime();

        // deleting user request
        mockMvc.perform(delete("/users/" + user.getUsername() + "/reset")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // try to get the user
        MvcResult result = mockMvc.perform(delete("/users/" + user.getUsername() + "/reset")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        User returnedUser = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);


        MvcResult resultTask = mockMvc.perform(get("/tasks/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        MvcResult resultTimeLog = mockMvc.perform(get("/time_logs/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // returned status should be 404 (Not Found)
        assertEquals(404, resultTimeLog.getResponse().getStatus(), "TimeLog was not deleted successfully");
        assertEquals(404, resultTask.getResponse().getStatus(), "Task was not deleted successfully");
        assertEquals(user.getUsername(), returnedUser.getUsername(), "User was not returned correctly");
    }
}
