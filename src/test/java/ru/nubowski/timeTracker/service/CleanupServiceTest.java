package ru.nubowski.timeTracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.nubowski.timeTracker.config.CleanupProperties;
import ru.nubowski.timeTracker.exception.CleanupFailedException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
        "cleanup.retentionPeriod=5",
        "cleanup.cronExpression=* * * * * ?"
})
public class CleanupServiceTest {
    @MockBean
    private TaskService taskService;
    @MockBean
    private UserService userService;
    @MockBean
    private TimeLogService timeLogService;
    @Autowired
    private CleanupProperties cleanupProperties;

    @Autowired
    private CleanupService cleanupService;

    @Test
    void testCallAllServices () {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(cleanupProperties.getRetentionPeriod());

        cleanupService.cleanup();

        verify(taskService, times(1)).deleteOldTasks(any(LocalDateTime.class));
        verify(userService, times(1)).deleteOldUsers(any(LocalDateTime.class));
        verify(timeLogService, times(1)).deleteOldTimeLogs(any(LocalDateTime.class));
    }

    @Test
    void testCleanupFailedException() {
        doThrow(new CleanupFailedException("deleteOldTasks")).when(taskService).deleteOldTasks(any(LocalDateTime.class));
        doThrow(new CleanupFailedException("deleteOldUsers")).when(userService).deleteOldUsers(any(LocalDateTime.class));
        doThrow(new CleanupFailedException("deleteOldTimeLogs")).when(timeLogService).deleteOldTimeLogs(any(LocalDateTime.class));

        assertThrows(CleanupFailedException.class, () -> cleanupService.cleanup());

        verify(taskService, times(1)).deleteOldTasks(any(LocalDateTime.class));
        verify(timeLogService, times(0)).deleteOldTimeLogs(any(LocalDateTime.class));
        verify(userService, times(0)).deleteOldUsers(any(LocalDateTime.class));
    }

    @Test
    void testGeneralException() {
        // if we passed all three internal calls..
        doNothing().when(taskService).deleteOldTasks(any(LocalDateTime.class));
        doNothing().when(userService).deleteOldUsers(any(LocalDateTime.class));
        doNothing().when(timeLogService).deleteOldTimeLogs(any(LocalDateTime.class));

        doThrow(new CleanupFailedException()).when(taskService).deleteOldTasks(any(LocalDateTime.class));

        assertThrows(CleanupFailedException.class, () -> cleanupService.cleanup());

        verify(taskService, times(1)).deleteOldTasks(any(LocalDateTime.class));
        verify(userService, times(1)).deleteOldUsers(any(LocalDateTime.class));
        verify(timeLogService, times(1)).deleteOldTimeLogs(any(LocalDateTime.class));
    }
}


