package ru.nubowski.timeTracker.userCaseTests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import ru.nubowski.timeTracker.controller.TimeLogControllerTest;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserTestCase {
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
    void testUserCreation () throws Exception {
        User user = new User();
        user.setUsername("time_user");
        user.setEmail("user@test.com");
        user.setDisplayName("nagibator9000");
        user = userService.saveUser(user);
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
        user = userService.saveUser(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                        .andExpect(status().isCreated());

        // create tasks

        Task task1 = new Task();
        task1.setName("testTask1");
        task1.setDescription("Some description to be sure it is working OK");
        task1.setUser(user);
        task1 = taskService.saveTask(task1);

        Task task2 = new Task();
        task2.setName("testTask2");
        task2.setDescription("Some description to be sure it is working OK");
        task2.setUser(user);
        task2 = taskService.saveTask(task2);

        Task task3 = new Task();
        task3.setName("testTask3");
        task3.setDescription("Some description to be sure it is working OK");
        task3.setUser(user);
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
        user = userService.saveUser(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        // create tasks
        Task task1 = new Task();
        task1.setName("testTask1");
        task1.setDescription("Some description to be sure it is working OK");
        task1.setUser(user);
        task1 = taskService.saveTask(task1);

        Task task2 = new Task();
        task2.setName("testTask2");
        task2.setDescription("Some description to be sure it is working OK");
        task2.setUser(user);
        task2 = taskService.saveTask(task2);

        // task1
        clockProvider.minusTime(Duration.ofHours(57));
        mockMvc.perform(post("/time_logs/start/" + task1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        clockProvider.plusTime(Duration.ofMinutes(257));
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
        String expectedInterval1 = String.format("%s - %s : %s",
                LocalDateTime.now().minusHours(57).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                LocalDateTime.now().minusHours(57).plusMinutes(257).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                task1.getName());
        String expectedInterval2 = String.format("%s - %s : %s",
                LocalDateTime.now().minusDays(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                LocalDateTime.now().minusDays(3).plusMinutes(49).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                task2.getName());
        List<String> expectedWorkIntervals = List.of(expectedInterval1, expectedInterval2);

        // Check if the expected and returned lists are equal
        assertEquals(expectedWorkIntervals, returnedWorkIntervals);
    }
}
