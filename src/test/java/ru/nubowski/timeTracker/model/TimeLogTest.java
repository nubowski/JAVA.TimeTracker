package ru.nubowski.timeTracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TimeLogTest {
    private TimeLog timeLog;

    @BeforeEach
    void setUp(){
        timeLog = new TimeLog();
    }

    @Test
    void testTimeLogId () {
        Long expcetedId = 5L;
        timeLog.setId(expcetedId);
        assertEquals(expcetedId, timeLog.getId());
    }

    @Test
    void testTimeLogStartAndEndTime() {
        LocalDateTime expectedStartTime = LocalDateTime.now();
        LocalDateTime expectedEndTime = LocalDateTime.now().plusHours(1);
        timeLog.setStartTime(expectedStartTime);
        timeLog.setEndTime(expectedEndTime);
        assertEquals(expectedStartTime, timeLog.getStartTime());
        assertEquals(expectedEndTime, timeLog.getEndTime());
    }

    @Test
    void testTimeLogEndedByUser() {
        timeLog.setEndedByUser(true);
        assertTrue(timeLog.isEndedByUser());
    }

    @Test
    void testTimeLogTaskAssociation() {
        Task task = new Task();
        task.setName("Test Task");
        // set and check
        timeLog.setTask(task);
        assertEquals(task, timeLog.getTask());
    }
}
