package ru.nubowski.timeTracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task();
    }

    @Test
    void testTaskId() {
        Long expectedId = 10L;
        task.setId(expectedId);
        assertEquals(expectedId, task.getId());
    }

    @Test
    void testTaskName() {
        String expectedName = "Test task";
        task.setName(expectedName);
        assertEquals(expectedName, task.getName());
    }

    @Test
    void testTaskCreatedAt() {
        LocalDateTime expectedCrteatedAt = LocalDateTime.now();
        task.setCreatedAt(expectedCrteatedAt);
        assertEquals(expectedCrteatedAt, task.getCreatedAt());
    }

    @Test
    void testTaskTimeLogs (){
        TimeLog timeLog = new TimeLog();
        task.getTimeLogs().add(timeLog);
        assertTrue(task.getTimeLogs().contains(timeLog));
    }

    @Test
    void testUserAssociation () {
        User user = new User();
        user.setUsername("TestUser"); // cross dependence apply
        task.setUser(user);
        assertEquals(user, task.getUser());
    }


}

