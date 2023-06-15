package ru.nubowski.timeTracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.nubowski.timeTracker.exception.TimeLogNotFoundException;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.TimeLog;
import ru.nubowski.timeTracker.repository.TimeLogRepository;
import ru.nubowski.timeTracker.service.impl.TimeLogService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TimeLogServiceTest {

    // trying to isolate
    @MockBean
    private TimeLogRepository timeLogRepository;

    @Autowired
    private TimeLogService timeLogService;

    @Test
    void testSaveTimeLog () {
        TimeLog timeLog = new TimeLog();
        timeLog.setStartTime(LocalDateTime.now());
        Task task = new Task();
        task.setName("task");
        task.setDescription("description");
        timeLog.setTask(task);

        when(timeLogRepository.save(any(TimeLog.class))).thenReturn(timeLog); // mockito

        TimeLog savedTimeLog = timeLogService.saveTimeLog(timeLog);

        assertNotNull(savedTimeLog);
        assertEquals(timeLog.getStartTime(), savedTimeLog.getStartTime());
        assertEquals(timeLog.getTask(), savedTimeLog.getTask());
    }

    @Test
    void testGetTimeLog () {
        TimeLog timeLog = new TimeLog();
        timeLog.setStartTime(LocalDateTime.now());
        Task task = new Task();
        timeLog.setTask(task);

        when(timeLogRepository.findById(anyLong())).thenReturn(Optional.of(timeLog));

        TimeLog timeLogFromDb = timeLogService.getTimeLog(1L);

        assertNotNull(timeLogFromDb);
        assertEquals(timeLog.getStartTime(), timeLogFromDb.getStartTime());
        assertEquals(timeLog.getTask(), timeLogFromDb.getTask());
    }

    @Test
    void testGetTimeLogNotFoundException() {
        when(timeLogRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(TimeLogNotFoundException.class, () -> {
            timeLogService.deleteTimeLog(1L);
        });
    }

    @Test
    void testDeleteTimeLogSuccess() {
        TimeLog timeLog = new TimeLog();
        timeLog.setId(1L);

        when(timeLogRepository.existsById(anyLong())).thenReturn(true);

        timeLogService.deleteTimeLog(timeLog.getId());

        verify(timeLogRepository, times(1)).deleteById(timeLog.getId());
    }

    @Test
    void testDeleteTimeLog() {
        when(timeLogRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(timeLogRepository).deleteById(anyLong());
        timeLogService.deleteTimeLog(1L);
        verify(timeLogRepository, times(1)).deleteById(anyLong());
    }
}
